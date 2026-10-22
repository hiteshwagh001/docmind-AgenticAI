import { useEffect, useRef, useState } from 'react'
import { ChevronDown, MessageSquare } from 'lucide-react'
import ChatMessages from './ChatMessages'
import ChatComposer from './ChatComposer'

function ChatWorkspace({ documents, selectedId, messages, sending, onSelectDocument, onAsk }) {
  const [query, setQuery] = useState('')
  const endRef = useRef(null)
  const selectedDocument = documents.find((document) => document.id === selectedId)

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages, sending])

  const submit = async (event) => {
    event.preventDefault()
    const question = query.trim()
    if (!question) return
    setQuery('')
    await onAsk(question)
  }

  return (
    <section className="panel flex min-h-[650px] flex-col overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4 sm:px-7">
        <div><div className="flex items-center gap-2"><MessageSquare size={19} className="text-indigo-600" /><h1 className="font-semibold">Ask Foxsec</h1></div><p className="mt-1 text-xs text-slate-500">{selectedDocument ? `Searching within ${selectedDocument.filename}` : 'Search across your entire knowledge library'}</p></div>
        <div className="relative"><select value={selectedId} onChange={(event) => onSelectDocument(event.target.value)} className="appearance-none rounded-xl border border-slate-200 bg-white py-2 pl-3 pr-9 text-sm outline-none transition focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100"><option value="">All documents</option>{documents.map((document) => <option key={document.id} value={document.id}>{document.filename}</option>)}</select><ChevronDown size={15} className="pointer-events-none absolute right-3 top-2.5 text-slate-400" /></div>
      </div>
      <ChatMessages messages={messages} sending={sending} endRef={endRef} />
      <ChatComposer query={query} sending={sending} onQueryChange={setQuery} onSubmit={submit} />
    </section>
  )
}

export default ChatWorkspace
