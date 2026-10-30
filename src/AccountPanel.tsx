import { useState, type FormEvent } from 'react'
import { api, type Account } from './api'
import { ArrowRight, LogIn, LogOut, UserRound, ShieldCheck } from 'lucide-react'
type Props = { account: Account | null; loading: boolean; syncStatus: string; onSignedIn: (token: string, account: Account) => Promise<void>; onSignOut: () => void }
export default function AccountPanel({account,loading,syncStatus,onSignedIn,onSignOut}: Props) {
 const [mode,setMode]=useState<'login'|'signup'>('login')
 const [name,setName]=useState('')
 const [email,setEmail]=useState('')
 const [password,setPassword]=useState('')
 const [busy,setBusy]=useState(false)
 const [error,setError]=useState('')
 async function submit(event: FormEvent<HTMLFormElement>) {
  event.preventDefault();setError('');setBusy(true)
  try {
   const result=mode==='signup' ? await api.signup(name,email,password) : await api.login(email,password)
   await onSignedIn(result.token,result.account)
   setPassword('')
  } catch(e){setError(e instanceof Error ? e.message : 'Unable to sign in')}
  finally{setBusy(false)}
 }
 return <section className="account-panel panel" aria-label="Learning account">
  <div className="account-panel-head"><div className="account-panel-icon"><UserRound size={21}/></div><div><span className="report-kicker">LEARNING ACCOUNT</span><h2>{account ? 'Your learning workspace' : 'Save your progress across devices'}</h2></div></div>
  {account ? <div className="account-signed-in"><p>Signed in as <strong>{account.displayName}</strong> ({account.email}).</p><p role="status">{syncStatus}</p><button className="secondary-button" onClick={onSignOut}><LogOut size={16}/> Sign out</button></div> :
   <><p className="account-panel-desc">Create an account or sign in to sync your lessons, quiz history and achievements to the Spring Boot database. You can continue as a guest without an account.</p><div className="account-switch" role="group" aria-label="Account action"><button type="button" aria-pressed={mode==='login'} className={mode==='login'?'selected':''} onClick={()=>{setMode('login');setError('')}}>Sign in</button><button type="button" aria-pressed={mode==='signup'} className={mode==='signup'?'selected':''} onClick={()=>{setMode('signup');setError('')}}>Create account</button></div><form className="account-form" onSubmit={submit}>{mode==='signup'&&<label>Display name<input required minLength={2} maxLength={80} autoComplete="name" value={name} onChange={e=>setName(e.target.value)}/></label>}<label>Email address<input required type="email" autoComplete="email" maxLength={254} value={email} onChange={e=>setEmail(e.target.value)}/></label><label>Password<input required type="password" minLength={mode==='signup'?12:1} maxLength={128} autoComplete={mode==='signup'?'new-password':'current-password'} value={password} onChange={e=>setPassword(e.target.value)}/></label>{mode==='signup'&&<p className="account-hint"><ShieldCheck size={15}/> Use a unique password of at least 12 characters.</p>}{error&&<p role="alert" className="account-error">{error}</p>}<button type="submit" className="primary-button" disabled={busy||loading}>{busy?'Please wait…':mode==='signup'?'Create account':'Sign in'} <ArrowRight size={16}/></button></form><p className="account-hint"><LogIn size={15}/> Guest progress stays on this browser. Signing in loads the account's saved progress; export a JSON backup first if you want to preserve guest work.</p></>}
 </section>
}
