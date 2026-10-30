import { useState } from 'react'
import { AlertTriangle, CheckCircle2, ExternalLink, Eye, ShieldCheck } from 'lucide-react'
type Finding = {level:'caution'|'info';message:string}
function inspect(raw:string): {hostname:string;findings:Finding[];valid:boolean} {
 const input=raw.trim()
 if (!input) return {hostname:'',findings:[],valid:false}
 if (input.length>2048) return {hostname:'',findings:[{level:'caution',message:'This example is unusually long. Use a shorter fictional URL.'}],valid:false}
 try {
  const url=new URL(input)
  if (!['http:','https:'].includes(url.protocol)) return {hostname:'',findings:[{level:'caution',message:'Only HTTP and HTTPS examples are supported.'}],valid:false}
  const hostname=url.hostname.toLowerCase()
  const findings:Finding[]=[]
  if (url.protocol==='http:') findings.push({level:'caution',message:'HTTP does not encrypt the connection. Avoid entering sensitive information.'})
  else findings.push({level:'info',message:'HTTPS encrypts the connection, but does not prove the website is legitimate.'})
  if (url.username||url.password) findings.push({level:'caution',message:'The URL contains an embedded username or password. This can disguise the actual destination.'})
  if (/^\d{1,3}(?:\.\d{1,3}){3}$/.test(hostname)||hostname.includes(':')) findings.push({level:'caution',message:'This address uses a numeric network address rather than a familiar domain name.'})
  if (hostname.startsWith('xn--')||hostname.includes('.xn--')) findings.push({level:'caution',message:'The hostname contains encoded international characters. Verify its spelling independently.'})
  if (hostname.split('.').length>=5) findings.push({level:'caution',message:'Several subdomain levels may make the actual domain harder to recognize.'})
  if (hostname.includes('--')||hostname.includes('verify-')||hostname.includes('-login')) findings.push({level:'caution',message:'The hostname contains wording often used in lookalike links. This is a clue, not proof of malicious intent.'})
  if (['bit.ly','tinyurl.com','t.co','shorturl.at'].includes(hostname)) findings.push({level:'caution',message:'A URL shortener can hide the destination. Do not assume a shortened link is trustworthy.'})
  if (url.port && !['80','443'].includes(url.port)) findings.push({level:'info',message:'A nonstandard port is present. This is not automatically dangerous, but worth understanding.'})
  if (url.search) findings.push({level:'info',message:'The link includes query parameters. Avoid sharing links that may contain personal tokens.'})
  if (findings.length===1&&url.protocol==='https:') findings.push({level:'info',message:'No additional indicators were detected by these simple checks. The destination may still be unsafe.'})
  return {hostname,findings,valid:true}
 } catch {return {hostname:'',findings:[{level:'caution',message:'Enter a complete fictional URL beginning with https:// or http://.'}],valid:false}}
}
const examples=[
 {label:'Ordinary encrypted address',value:'https://library.example/catalog'},
 {label:'Lookalike subdomain',value:'https://account.login.security-check.example/verify'},
 {label:'Unencrypted connection',value:'http://student-services.example/login'},
 {label:'Misleading user information',value:'https://trusted.example@unfamiliar.example/sign-in'},
]
export default function URLSafety(){
 const [input,setInput]=useState(examples[0].value)
 const result=inspect(input)
 return <section className="url-lab">
 <div className="panel url-lab-main"><span className="report-kicker">URL AWARENESS · SAFE PRACTICE</span><h2>Understand what a link is telling you</h2><p>Explore fictional links and identify visible clues. This tool never visits, scans, or opens the address you enter. It is a learning aid, not a safety verdict.</p>
 <div className="url-examples">{examples.map(example=><button key={example.label} type="button" className={input===example.value?'selected':''} onClick={()=>setInput(example.value)}>{example.label}</button>)}</div>
 <label className="field-label" htmlFor="url-example">Fictional URL for inspection</label><input id="url-example" className="url-input" type="text" value={input} onChange={event=>setInput(event.target.value)} spellCheck={false} autoComplete="off" maxLength={2048} placeholder="https://example.com/path"/>
 {input.trim()&&<div className="url-results" aria-live="polite"><h3><Eye size={18}/> What the URL reveals</h3>{result.valid&&<div className="url-host"><span>HOSTNAME</span><strong>{result.hostname}</strong></div>}{result.findings.map((item,index)=><div key={index} className={`url-finding ${item.level}`}>{item.level==='caution'?<AlertTriangle size={17}/>:<CheckCircle2 size={17}/>}<span>{item.message}</span></div>)}</div>}
 </div><aside className="panel url-lab-guide"><ShieldCheck size={28}/><h3>How to check links safely</h3><p>Read the hostname carefully, especially the words immediately before the domain ending. A familiar brand name elsewhere in a link is not proof of ownership.</p><p>When a message requests payment, sign-in details, or a verification code, navigate to the service yourself through a trusted bookmark or app.</p><p>HTTPS only tells you that the connection is encrypted. It does not guarantee that a website or offer is legitimate.</p><div className="url-lesson-note"><ExternalLink size={17}/><span>No external requests are made by this exercise. Do not paste private links or access tokens.</span></div></aside></section>
}
