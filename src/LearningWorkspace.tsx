import { useEffect, useState } from 'react'
import { learningApi, readToken, type Account, type ContentItem, type AdminAnalytics, type StudentDashboard } from './api'
import { Award, BookOpen, CheckCircle2, Plus, Save, Trash2, RefreshCw, ShieldCheck } from 'lucide-react'
type Props={account:Account|null}
const types=['COURSE','LESSON','QUIZ','PHISHING','CHALLENGE'] as const
const blank=()=>({type:'COURSE' as ContentItem['type'],title:'',summary:'',body:{text:''} as ContentItem['body'],published:false})
export default function LearningWorkspace({account}:Props){
 const [catalog,setCatalog]=useState<ContentItem[]>([])
 const [items,setItems]=useState<ContentItem[]>([])
 const [dashboard,setDashboard]=useState<StudentDashboard|null>(null)
 const [analytics,setAnalytics]=useState<AdminAnalytics|null>(null)
 const [editing,setEditing]=useState<number|null>(null)
 const [form,setForm]=useState<Omit<ContentItem,'id'|'updatedAt'>>(blank())
 const [rawBody,setRawBody]=useState('{"text":""}')
 const [answers,setAnswers]=useState<Record<number,Record<number,number>>>({})
 const [result,setResult]=useState<Record<number,string>>({})
 const [error,setError]=useState('')
 const [busy,setBusy]=useState(false)
 const token=readToken()
 const admin=account?.role==='ADMIN'
 async function refresh(){
  try{
   const publicItems=await learningApi.catalog();setCatalog(publicItems)
   if(token&&admin){const [all,stats]=await Promise.all([learningApi.adminContent(token),learningApi.analytics(token)]);setItems(all);setAnalytics(stats)}
   if(token&&account?.role==='STUDENT')setDashboard(await learningApi.studentDashboard(token))
  }catch(e){setError(e instanceof Error?e.message:'Could not load workspace')}
 }
 useEffect(()=>{void refresh()},[account?.id,account?.role])
 function startEdit(item:ContentItem){setEditing(item.id);setForm({type:item.type,title:item.title,summary:item.summary,body:item.body,published:item.published});setRawBody(JSON.stringify(item.body,null,2));setError('')}
 function newItem(){setEditing(null);setForm(blank());setRawBody('{"text":""}');setError('')}
 async function perform(action:()=>Promise<unknown>){setBusy(true);setError('');try{await action();await refresh()}catch(e){setError(e instanceof Error?e.message:'Action failed')}finally{setBusy(false)}}
 async function save(){
  if(!token)return
  await perform(async()=>{
   const body=JSON.parse(rawBody) as ContentItem['body']
   if(!body || Array.isArray(body) || typeof body!=='object')throw new Error('Body must be a JSON object')
   const payload={...form,body}
   if(editing!==null)await learningApi.update(token,editing,payload)
   else await learningApi.create(token,payload)
   newItem()
  })
 }
 function downloadCertificate(course:string){
  if(!account)return
  const escape=(value:string)=>value.replace(/[&<>"]/g,char=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[char]||char))
  const html='<html lang="en"><head><meta charset="utf-8"><title>CyberShield Completion Record</title><style>body{font-family:Arial,sans-serif;padding:50px;color:#294e3f}.sheet{border:8px double #34765e;padding:60px;text-align:center}h1{font-size:34px}p{line-height:1.8}.note{font-size:12px;margin-top:45px}</style></head><body><div class="sheet"><p>CYBERSHIELD LEARNING RECORD</p><h1>Course Completion</h1><p>This record acknowledges</p><h2>'+escape(account.displayName)+'</h2><p>marked the following course complete:</p><h2>'+escape(course)+'</h2><p>Issued '+escape(new Date().toLocaleDateString())+'</p><p class="note">Self-reported completion, not a verified certification or professional qualification.</p></div></body></html>'
  const url=URL.createObjectURL(new Blob([html],{type:'text/html;charset=utf-8'}))
  const a=document.createElement('a');a.href=url;a.download='cybershield-completion-record.html';a.click();window.setTimeout(()=>URL.revokeObjectURL(url),1000)
 }
 function downloadStats(){
  if(!analytics)return
  const rows=[['Metric','Value'],['Students',analytics.students],['Active learners (30 days)',analytics.activeLearners30Days],['Courses',analytics.courses],['Enrollments',analytics.enrollments],['Completions',analytics.completions],['Attempts',analytics.assessmentAttempts],...analytics.difficultTopics.map(t=>[t.title+' accuracy',t.accuracy+'%'])]
  const csv=rows.map(row=>row.map(cell=>'"'+String(cell).replaceAll('"','""')+'"').join(',')).join('\r\n')
  const url=URL.createObjectURL(new Blob([csv],{type:'text/csv;charset=utf-8'}))
  const a=document.createElement('a');a.href=url;a.download='cybershield-analytics.csv';a.click();URL.revokeObjectURL(url)
 }
 return <div className="workspace-full">
  <header className="workspace-heading"><div><span className="report-kicker">{admin?'ADMINISTRATOR WORKSPACE':'STUDENT LEARNING WORKSPACE'}</span><h2>{admin?'Manage learning and measure impact':'Explore your learning catalog'}</h2><p>{admin?'Publish lessons, courses, quizzes and safe practice activities without editing source code.':'Enroll in courses, practise safely and review your results.'}</p></div><button className="secondary-button" disabled={busy} onClick={()=>void refresh()}><RefreshCw size={15}/> Refresh</button></header>
  {error&&<p className="workspace-error" role="alert">{error}</p>}
  {!account&&<p className="workspace-note">Browse published learning content as a guest. Sign in with a student account to enroll and save assessment results.</p>}
  {admin&&analytics&&<><div className="workspace-metrics">{[['Students',analytics.students],['Active learners (30 days)',analytics.activeLearners30Days],['Published courses',analytics.courses],['Enrollments',analytics.enrollments],['Completions',analytics.completions],['Assessment attempts',analytics.assessmentAttempts]].map(([label,value])=><div className="panel" key={label}><span>{label}</span><strong>{value}</strong></div>)}</div><section className="panel workspace-card"><div className="workspace-card-title"><h3>Assessment insights</h3><button className="secondary-button" onClick={downloadStats}>Export CSV</button></div>{analytics.difficultTopics.length?analytics.difficultTopics.map(topic=><div className="workspace-result" key={topic.title}><span>{topic.title} · {topic.attempts} attempts</span><strong>{topic.accuracy}% correct</strong></div>):<p>No assessment attempts recorded yet.</p>}</section></>}
  {admin&&<section className="panel workspace-card"><div className="workspace-card-title"><h3>Content management</h3><button className="secondary-button" onClick={newItem}><Plus size={15}/> New content</button></div><div className="workspace-editor"><label>Content type<select value={form.type} disabled={editing!==null} onChange={e=>{const type=e.target.value as ContentItem['type'];setForm(p=>({...p,type}));setRawBody(types.includes(type)&&['QUIZ','PHISHING','CHALLENGE'].includes(type)?'{"questions":[{"question":"Which option is safest?","options":["Verify independently","Share your password"],"correct":0,"explanation":"Always verify independently."}]}':'{"text":""}')}}>{types.map(t=><option key={t}>{t}</option>)}</select></label><label>Title<input value={form.title} maxLength={160} onChange={e=>setForm(p=>({...p,title:e.target.value}))}/></label><label>Summary<input value={form.summary} maxLength={1000} onChange={e=>setForm(p=>({...p,summary:e.target.value}))}/></label><label>Content JSON<textarea rows={8} spellCheck={false} value={rawBody} onChange={e=>setRawBody(e.target.value)}/></label><label className="workspace-checkbox"><input type="checkbox" checked={form.published} onChange={e=>setForm(p=>({...p,published:e.target.checked}))}/> Published and visible to students</label><button className="primary-button" disabled={busy||!form.title.trim()} onClick={()=>void save()}><Save size={16}/> {editing?'Save changes':'Create content'}</button></div><div className="workspace-content-list">{items.map(item=><div className="workspace-content-row" key={item.id}><div><strong>{item.title}</strong><span>{item.type} · {item.published?'Published':'Draft'}</span></div><div><button className="secondary-button" onClick={()=>startEdit(item)}>Edit</button><button className="secondary-button" disabled={busy} onClick={()=>{if(token&&window.confirm('Delete this content? Existing student records may prevent deletion.'))void perform(()=>learningApi.remove(token,item.id))}}><Trash2 size={15}/></button></div></div>)}</div></section>}
  {dashboard&&<><div className="workspace-metrics">{[['Enrolled courses',dashboard.enrollments.length],['Completed courses',dashboard.completed],['Course progress',dashboard.enrollments.length?Math.round(dashboard.completed/dashboard.enrollments.length*100)+'%':'0%'],['Assessment attempts',dashboard.attempts.length],['Activity streak (UTC days)',dashboard.streak]].map(([label,value])=><div className="panel" key={label}><span>{label}</span><strong>{value}</strong></div>)}</div><section className="panel workspace-card"><h3>Your learning journey</h3><p>{dashboard.recommendation}</p>{dashboard.enrollments.map(e=><div className="workspace-content-row" key={e.courseId}><div><strong>{e.title}</strong><span>{e.completed?'Completed':'In progress'}</span></div>{!e.completed&&token&&<button className="secondary-button" disabled={busy} onClick={()=>void perform(()=>learningApi.complete(token,e.courseId))}><CheckCircle2 size={15}/> Mark complete</button>}</div>)}</section><section className="panel workspace-card"><h3><Award size={17}/> Learning badges</h3><div className="workspace-badges">{[{title:'First step',earned:dashboard.enrollments.length>0,description:'Enroll in a course'},{title:'Knowledge explorer',earned:dashboard.attempts.length>0,description:'Complete an assessment'},{title:'Strong start',earned:dashboard.attempts.some(a=>a.total>0&&a.score/a.total>=0.8),description:'Score at least 80% on an assessment'},{title:'Course finisher',earned:dashboard.completed>0,description:'Complete a course'}].map(badge=><div className={badge.earned?'workspace-badge earned':'workspace-badge'} key={badge.title}><Award size={18}/><strong>{badge.title}</strong><span>{badge.earned?'Earned':badge.description}</span></div>)}</div></section><section className="panel workspace-card"><h3>Assessment history</h3>{dashboard.attempts.length?dashboard.attempts.map(a=><div className="workspace-result" key={a.id}><span>{a.title} · {new Date(a.attemptedAt).toLocaleDateString()}</span><strong>{a.score}/{a.total}</strong></div>):<p>Complete a published quiz to see your results here.</p>}</section><section className="panel workspace-card"><h3><Award size={17}/> Completion records</h3>{dashboard.certificates.length?dashboard.certificates.map(name=><div className="workspace-content-row" key={name}><span><ShieldCheck size={15}/> {name} — self-reported completion</span><button className="secondary-button" onClick={()=>downloadCertificate(name)}>Download record</button></div>):<p>Complete an enrolled course to earn a completion record.</p>}</section></>}
  <section className="workspace-catalog"><h3><BookOpen size={19}/> Published learning catalog</h3>{!catalog.length&&<p>No published content yet. An administrator can create the first course or assessment.</p>}<div className="workspace-catalog-grid">{catalog.map(item=><article className="panel workspace-item" key={item.id}><span className="report-kicker">{item.type}</span><h3>{item.title}</h3><p>{item.summary}</p>{item.body.text&&<div className="workspace-text">{item.body.text}</div>}{item.type==='COURSE'&&account?.role==='STUDENT'&&token&&<button className="primary-button" disabled={busy||dashboard?.enrollments.some(e=>e.courseId===item.id)} onClick={()=>void perform(()=>learningApi.enroll(token,item.id))}>{dashboard?.enrollments.some(e=>e.courseId===item.id)?'Enrolled':'Enroll in course'}</button>}{['QUIZ','PHISHING','CHALLENGE'].includes(item.type)&&item.body.questions&&<div className="workspace-questions">{item.body.questions.map((question,index)=><fieldset key={index}><legend>{index+1}. {question.question}</legend>{question.options.map((option,choice)=><label key={choice}><input type="radio" name={'assessment-'+item.id+'-'+index} checked={answers[item.id]?.[index]===choice} onChange={()=>setAnswers(p=>({...p,[item.id]:{...p[item.id],[index]:choice}}))}/>{option}</label>)}</fieldset>)}{account?.role==='STUDENT'&&token?<button className="primary-button" disabled={busy||item.body.questions.some((_,i)=>answers[item.id]?.[i]===undefined)} onClick={()=>void perform(async()=>{const response=await learningApi.submit(token,item.id,item.body.questions!.map((_,i)=>answers[item.id][i]));setResult(p=>({...p,[item.id]:`Score: ${response.score}/${response.total}. Review the relevant lesson and try again to improve.`}))})}>Submit answers</button>:<p className="workspace-note">Sign in as a student to submit and save assessment results.</p>}{result[item.id]&&<p role="status">{result[item.id]}</p>}</div>}</article>)}</div></section>
 </div>
}
