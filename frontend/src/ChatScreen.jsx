import { useEffect, useLayoutEffect, useRef, useState } from 'react'
import { clearToken, createChat, getChat, listChats, streamMessage } from './api.js'
import { chatTitle, errorText, formatTime, renderMarkdown } from './text.js'

export function ChatScreen({ user, onLogout }) {
  const [chats, setChats] = useState([])
  const [cursor, setCursor] = useState(null)
  const [query, setQuery] = useState('')
  const [chat, setChat] = useState(null)
  const [draft, setDraft] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const abortRef = useRef(null)
  const searchTimer = useRef(null)
  const transcriptRef = useRef(null)
  const scrollFix = useRef(null)

  useEffect(() => {
    refresh('')
    return () => abortRef.current?.abort()
  }, [])

  useEffect(() => {
    const box = transcriptRef.current
    if (box && scrollFix.current == null) box.scrollTop = box.scrollHeight
  }, [chat?.id, chat?.messages?.at(-1)?.content, sending])

  useLayoutEffect(() => {
    const box = transcriptRef.current
    if (!box || scrollFix.current == null) return
    box.scrollTop = box.scrollHeight - scrollFix.current
    scrollFix.current = null
  }, [chat?.messages])

  async function refresh(nextQuery, appendCursor) {
    const page = await listChats({ query: nextQuery, cursor: appendCursor })
    setChats((items) => (appendCursor ? items.concat(page.items) : page.items))
    setCursor(page.nextCursor)
  }

  function logout() {
    abortRef.current?.abort()
    clearToken()
    onLogout()
  }

  async function open(chatId) {
    abortRef.current?.abort()
    setSending(false)
    setError('')
    setChat(await getChat(chatId))
    setSidebarOpen(false)
  }

  async function loadOlder() {
    if (!chat?.nextCursor) return
    const box = transcriptRef.current
    scrollFix.current = box ? box.scrollHeight : 0
    const page = await getChat(chat.id, chat.nextCursor)
    setChat((current) => ({
      ...current,
      messages: page.messages.concat(current.messages.filter((item) => !String(item.id).startsWith('pending'))),
      nextCursor: page.nextCursor,
    }))
  }

  async function startChat() {
    const created = await createChat()
    setQuery('')
    const page = await listChats({})
    setChats(page.items.some((item) => item.id === created.id) ? page.items : [created, ...page.items])
    setCursor(page.nextCursor)
    setChat({ ...created, messages: [], nextCursor: null })
    setError('')
    setSidebarOpen(false)
  }

  function onSearch(value) {
    setQuery(value)
    clearTimeout(searchTimer.current)
    searchTimer.current = setTimeout(() => {
      refresh(value.trim()).catch((reason) => setError(errorText(reason)))
    }, 250)
  }

  async function send(event) {
    event.preventDefault()
    const content = draft.trim()
    if (!content || !chat || sending) return
    const chatId = chat.id
    setDraft('')
    setError('')
    setSending(true)
    setChat((current) => ({
      ...current,
      messages: [
        ...current.messages,
        { id: 'pending-user', role: 'USER', content },
        { id: 'pending-bot', role: 'ASSISTANT', content: '' },
      ],
    }))
    const controller = new AbortController()
    abortRef.current = controller
    let assembled = ''
    try {
      await streamMessage(chatId, content, {
        signal: controller.signal,
        onEvent(sse) {
          if (sse.name === 'delta') {
            assembled += sse.data.text || ''
            const text = assembled
            setChat((current) => ({
              ...current,
              messages: current.messages.map((item) => (item.id === 'pending-bot' ? { ...item, content: text } : item)),
            }))
          } else if (sse.name === 'title') {
            setChat((current) => (current?.id === chatId ? { ...current, title: sse.data.title } : current))
            setChats((items) => items.map((item) => (item.id === chatId ? { ...item, title: sse.data.title } : item)))
          } else if (sse.name === 'error') {
            throw new Error(sse.data.message || 'AI service is unavailable')
          }
        },
      })
      setChat(await getChat(chatId))
      await refresh(query.trim())
    } catch (reason) {
      if (reason.name === 'AbortError') return
      setError(errorText(reason))
      try {
        setChat(await getChat(chatId))
      } catch {
        setChat(null)
      }
    } finally {
      setSending(false)
      abortRef.current = null
    }
  }

  return (
    <div className="shell">
      <aside id="sidebar" className={sidebarOpen ? 'open' : undefined}>
        <div className="me">
          <div>
            <strong>{user.login}</strong>
            <span>{user.email}</span>
          </div>
          <button type="button" onClick={logout}>Выйти</button>
        </div>
        <button type="button" className="primary" onClick={startChat}>Новый чат</button>
        <label className="search">Поиск
          <input type="search" value={query} placeholder="Заголовок" autoComplete="off" onChange={(event) => onSearch(event.target.value)} />
        </label>
        <ul id="chat-list">
          {!chats.length && <li className="empty">{query.trim() ? 'Ничего не найдено' : 'Чатов пока нет'}</li>}
          {chats.map((item) => (
            <li key={item.id}>
              <button type="button" className={item.id === chat?.id ? 'active' : undefined} onClick={() => open(item.id)}>
                <strong>{chatTitle(item)}</strong>
                <small>{formatTime(item.updatedAt)}</small>
              </button>
            </li>
          ))}
        </ul>
        <button type="button" id="more-chats" hidden={!cursor} onClick={() => refresh(query.trim(), cursor)}>Ещё чаты</button>
      </aside>
      <section className="thread">
        <header className="thread-head">
          <button type="button" className="toggle-chats" onClick={() => setSidebarOpen((open) => !open)}>Чаты</button>
          <h2 id="thread-title">{chat ? chatTitle(chat) : 'Выберите чат'}</h2>
        </header>
        <div id="transcript" className="transcript" ref={transcriptRef} aria-live="polite">
          {!chat && <p className="empty">Создайте чат или выберите его в списке.</p>}
          {chat?.nextCursor && <button type="button" onClick={loadOlder}>Более ранние сообщения</button>}
          {chat && !chat.messages.length && <p className="empty">Напишите первое сообщение. Заголовок появится после ответа.</p>}
          {chat?.messages.map((message) => (
            <article key={message.id} className={message.role === 'USER' ? 'message user' : 'message assistant'}>
              <p className="who">{message.role === 'USER' ? 'Вы' : 'Бот'}</p>
              {message.role === 'USER'
                ? <div className="body">{message.content}</div>
                : <div className="md" dangerouslySetInnerHTML={{ __html: message.content ? renderMarkdown(message.content) : '…' }} />}
            </article>
          ))}
        </div>
        <form id="composer" onSubmit={send}>
          <p className="form-error" role="alert">{error}</p>
          <textarea
            id="draft"
            rows={3}
            maxLength={16000}
            placeholder="Напишите сообщение, Ctrl+Enter — отправить"
            value={draft}
            disabled={!chat || sending}
            onChange={(event) => setDraft(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
                event.preventDefault()
                event.currentTarget.form?.requestSubmit()
              }
            }}
          />
          <button id="send" type="submit" disabled={!chat || sending || !draft.trim()}>Отправить</button>
        </form>
      </section>
    </div>
  )
}
