import { useEffect, useState } from 'react'
import { clearToken, getToken, me } from './api.js'
import { AuthScreen } from './AuthScreen.jsx'
import { ChatScreen } from './ChatScreen.jsx'

export function App() {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    let active = true
    if (!getToken()) {
      setReady(true)
      return undefined
    }
    me()
      .then((profile) => {
        if (active) setUser(profile)
      })
      .catch(() => clearToken())
      .finally(() => {
        if (active) setReady(true)
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const drop = () => setUser(null)
    window.addEventListener('aichat:unauthorized', drop)
    return () => window.removeEventListener('aichat:unauthorized', drop)
  }, [])

  if (!ready) return null
  if (!user) return <AuthScreen onEnter={setUser} />
  return <ChatScreen user={user} onLogout={() => setUser(null)} />
}
