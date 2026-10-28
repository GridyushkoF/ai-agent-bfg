const API = import.meta.env.VITE_API_BASE ?? ''
const TOKEN_KEY = 'aichat.token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  constructor(message, status, code, details) {
    super(message)
    this.status = status
    this.code = code
    this.details = details ?? {}
  }
}

function authHeaders(extra) {
  const headers = new Headers(extra)
  const token = getToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  return headers
}

async function toError(response) {
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  if (response.status === 401 && getToken()) {
    clearToken()
    window.dispatchEvent(new Event('aichat:unauthorized'))
  }
  return new ApiError(payload?.message || 'Запрос не выполнен', response.status, payload?.code, payload?.details)
}

async function request(path, options = {}) {
  const headers = authHeaders(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json')
  const response = await fetch(API + path, { ...options, headers })
  if (!response.ok) throw await toError(response)
  if (response.status === 204) return null
  return response.json()
}

export function register(body) {
  return request('/api/v1/auth/register', { method: 'POST', body: JSON.stringify(body) })
}

export function login(body) {
  return request('/api/v1/auth/login', { method: 'POST', body: JSON.stringify(body) })
}

export function me() {
  return request('/api/v1/users/me')
}

export function listChats({ query, cursor } = {}) {
  const params = new URLSearchParams()
  if (query) params.set('query', query)
  if (cursor) params.set('cursor', cursor)
  const suffix = params.size ? `?${params}` : ''
  return request(`/api/v1/chats${suffix}`)
}

export function createChat() {
  return request('/api/v1/chats', { method: 'POST' })
}

export function getChat(chatId, cursor) {
  const suffix = cursor ? `?${new URLSearchParams({ cursor })}` : ''
  return request(`/api/v1/chats/${chatId}${suffix}`)
}

export async function streamMessage(chatId, content, { signal, onEvent }) {
  const response = await fetch(`${API}/api/v1/chats/${chatId}/messages`, {
    method: 'POST',
    headers: authHeaders({ 'Content-Type': 'application/json', Accept: 'text/event-stream' }),
    body: JSON.stringify({ content }),
    signal,
  })
  if (!response.ok) throw await toError(response)
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true }).replace(/\r/g, '')
    let split = buffer.indexOf('\n\n')
    while (split >= 0) {
      const event = parseEvent(buffer.slice(0, split))
      buffer = buffer.slice(split + 2)
      if (event) onEvent(event)
      split = buffer.indexOf('\n\n')
    }
  }
}

function parseEvent(block) {
  let name = 'message'
  const data = []
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) name = line.slice(6).trim()
    else if (line.startsWith('data:')) data.push(line.slice(5).trim())
  }
  if (!data.length) return null
  const text = data.join('\n')
  try {
    return { name, data: JSON.parse(text) }
  } catch {
    return { name, data: { message: text } }
  }
}
