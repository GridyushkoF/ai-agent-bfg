import DOMPurify from 'dompurify'
import { marked } from 'marked'
import { ApiError } from './api.js'

marked.use({ gfm: true, breaks: true })

const CODE_TEXT = {
  LOGIN_TAKEN: 'Логин уже занят',
  EMAIL_TAKEN: 'Почта уже занята',
  UNAUTHORIZED: 'Неверный логин или пароль',
  NOT_FOUND: 'Чат не найден',
  TECHNICAL: 'Внутренняя ошибка',
}

export function errorText(error) {
  if (error instanceof ApiError && CODE_TEXT[error.code]) return CODE_TEXT[error.code]
  if (error?.message === 'AI service is unavailable') return 'Сервис ИИ сейчас недоступен. Сообщение не сохранено.'
  return error?.message || 'Запрос не выполнен'
}

export function chatTitle(chat) {
  return chat?.title || 'Новый чат'
}

export function formatTime(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('ru', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export function renderMarkdown(text) {
  return DOMPurify.sanitize(marked.parse(text || ''))
}
