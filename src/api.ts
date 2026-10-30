export type Account = { id: number; email: string; displayName: string; role: 'ADMIN' | 'STUDENT' }
type LoginResponse = { token: string; account: Account }
const BASE = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')
const TOKEN_KEY = 'cybershield-session'
export const readToken = () => sessionStorage.getItem(TOKEN_KEY)
export const saveToken = (token: string) => sessionStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => sessionStorage.removeItem(TOKEN_KEY)
export async function request<T>(path: string, method = 'GET', data?: unknown, token?: string): Promise<T> {
  let response: Response
  try { response = await fetch(BASE + path, {
    method, headers: { ...(data === undefined ? {} : {'Content-Type':'application/json'}), ...(token ? {Authorization: 'Bearer ' + token} : {}) },
    body: data === undefined ? undefined : JSON.stringify(data),
    cache:'no-store'
  }) } catch { throw new Error('Cannot reach the Cyber Shield backend. Check that Spring Boot is running and VITE_API_URL points to its public /api endpoint.') }
  if (!response.ok) {
    const error = await response.json().catch(() => ({}))
    throw new Error(typeof error.error === 'string' ? error.error : 'Request failed (' + response.status + ')')
  }
  if (response.status === 204) return undefined as T
  const type = response.headers.get('content-type') || ''
  if (!type.includes('application/json')) throw new Error('The API returned a web page instead of JSON. Check VITE_API_URL and the backend deployment.')
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

export type ContentItem = {id:number;type:'COURSE'|'LESSON'|'QUIZ'|'PHISHING'|'CHALLENGE';title:string;summary:string;body:{text?:string;questions?:{question:string;options:string[];correct?:number;explanation?:string}[]};published:boolean;updatedAt:string}
export type Enrollment = {courseId:number;title:string;completed:boolean;enrolledAt:string}
export type Attempt = {id:number;assessmentId:number;title:string;score:number;total:number;attemptedAt:string}
export type StudentDashboard = {enrollments:Enrollment[];attempts:Attempt[];completed:number;streak:number;certificates:string[];recommendation:string}
export type AdminAnalytics = {students:number;activeLearners30Days:number;courses:number;enrollments:number;completions:number;assessmentAttempts:number;difficultTopics:{title:string;attempts:number;accuracy:number}[]}
export const learningApi = {
 catalog:()=>request<ContentItem[]>('/catalog'),
 adminContent:(token:string)=>request<ContentItem[]>('/admin/content','GET',undefined,token),
 create:(token:string,item:Omit<ContentItem,'id'|'updatedAt'>)=>request<ContentItem>('/admin/content','POST',item,token),
 update:(token:string,id:number,item:Omit<ContentItem,'id'|'updatedAt'>)=>request<ContentItem>('/admin/content/'+id,'PUT',item,token),
 remove:(token:string,id:number)=>request<void>('/admin/content/'+id,'DELETE',undefined,token),
 analytics:(token:string)=>request<AdminAnalytics>('/admin/analytics','GET',undefined,token),
 studentDashboard:(token:string)=>request<StudentDashboard>('/student/dashboard','GET',undefined,token),
 enroll:(token:string,id:number)=>request<Enrollment>('/student/enroll/'+id,'POST',{},token),
 complete:(token:string,id:number)=>request<Enrollment>('/student/complete/'+id,'POST',{},token),
 submit:(token:string,id:number,answers:number[])=>request<Attempt>('/student/submit/'+id,'POST',{answers},token),
}
