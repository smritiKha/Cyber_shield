export type Account = { id: number; email: string; displayName: string }
type LoginResponse = { token: string; account: Account }
const BASE = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')
const TOKEN_KEY = 'cybershield-session'
export const readToken = () => sessionStorage.getItem(TOKEN_KEY)
export const saveToken = (token: string) => sessionStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => sessionStorage.removeItem(TOKEN_KEY)
async function request<T>(path: string, method = 'GET', data?: unknown, token?: string): Promise<T> {
  const response = await fetch(BASE + path, {
    method, headers: { ...(data === undefined ? {} : {'Content-Type':'application/json'}), ...(token ? {Authorization: 'Bearer ' + token} : {}) },
    body: data === undefined ? undefined : JSON.stringify(data),
    cache:'no-store'
  })
  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    throw new Error(typeof error.error === 'string' ? error.error : 'Request failed (' + response.status + ')')
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
export const api = {
  signup: (displayName: string, email: string, password: string) => request<LoginResponse>('/auth/signup','POST',{displayName,email,password}),
  login: (email: string, password: string) => request<LoginResponse>('/auth/login','POST',{email,password}),
  me: (token: string) => request<Account>('/auth/me','GET',undefined,token),
  logout: (token: string) => request<void>('/auth/logout','POST',undefined,token),
  load: (token: string) => request<{progress:unknown;updatedAt:string|null}>('/progress','GET',undefined,token),
  save: (token: string, progress: unknown) => request<{progress:unknown;updatedAt:string}>('/progress','PUT',{progress},token),
}
