import { useState } from 'react'
import { login, register, setToken } from './api.js'
import { errorText } from './text.js'

const EMPTY_ERRORS = { login: '', email: '', password: '', form: '' }

export function AuthScreen({ onEnter }) {
  const [mode, setMode] = useState('login')
  const [loginName, setLoginName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState(EMPTY_ERRORS)
  const [pending, setPending] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setErrors(EMPTY_ERRORS)
    setPending(true)
    try {
      const auth = mode === 'register'
        ? await register({ login: loginName.trim(), email: email.trim(), password })
        : await login({ login: loginName.trim(), password })
      setToken(auth.accessToken)
      onEnter(auth.user)
    } catch (error) {
      const details = error.details || {}
      const known = ['login', 'email', 'password'].filter((field) => details[field])
      setErrors({
        login: details.login ? String(details.login) : '',
        email: details.email ? String(details.email) : '',
        password: details.password ? String(details.password) : '',
        form: known.length ? '' : errorText(error),
      })
    } finally {
      setPending(false)
    }
  }

  return (
    <main className={mode === 'register' ? 'auth register' : 'auth'}>
      <form className="card" onSubmit={submit}>
        <p className="mark">ai chat</p>
        <h1>Переписка с ботом</h1>
        <p className="lede">
          {mode === 'login' ? 'Войдите по логину и паролю.' : 'Логин, почта и пароль. Код на почту не нужен.'}
        </p>
        <div className="switch" role="tablist">
          <button type="button" role="tab" aria-selected={mode === 'login'} onClick={() => setMode('login')}>Вход</button>
          <button type="button" role="tab" aria-selected={mode === 'register'} onClick={() => setMode('register')}>Регистрация</button>
        </div>
        <label className="field">Логин
          <input
            value={loginName}
            onChange={(event) => setLoginName(event.target.value)}
            required
            minLength={3}
            maxLength={64}
            pattern={mode === 'register' ? '[A-Za-z0-9_.-]+' : undefined}
            autoComplete="username"
          />
        </label>
        <p className="field-error">{errors.login}</p>
        {mode === 'register' && (
          <>
            <label className="field">Почта
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
                maxLength={255}
                autoComplete="email"
              />
            </label>
            <p className="field-error">{errors.email}</p>
          </>
        )}
        <label className="field">Пароль
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            minLength={8}
            maxLength={72}
            autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
          />
        </label>
        <p className="field-error">{errors.password}</p>
        <p className="form-error" role="alert">{errors.form}</p>
        <button id="auth-submit" type="submit" disabled={pending}>
          {mode === 'login' ? 'Войти' : 'Создать аккаунт'}
        </button>
      </form>
    </main>
  )
}
