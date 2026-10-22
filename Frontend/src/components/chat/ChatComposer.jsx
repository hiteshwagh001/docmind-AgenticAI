import { Send } from 'lucide-react'

function ChatComposer({ query, sending, onQueryChange, onSubmit }) {
  const handleKeyDown = (event) => {
    if (event.key === 'Enter' && !event.shiftKey) onSubmit(event)
  }

  return (
    <div className="border-t border-slate-100 bg-white p-4 sm:p-5">
      <form onSubmit={onSubmit} className="flex items-end gap-2 rounded-2xl border border-slate-200 bg-slate-50 p-2 transition focus-within:border-indigo-400 focus-within:ring-4 focus-within:ring-indigo-100">
        <textarea value={query} onChange={(event) => onQueryChange(event.target.value)} onKeyDown={handleKeyDown} rows="1" placeholder="Ask anything about your documents…" className="max-h-32 min-h-10 flex-1 resize-none bg-transparent px-2 py-2.5 text-sm outline-none placeholder:text-slate-400" />
        <button disabled={!query.trim() || sending} className="grid h-10 w-10 place-items-center rounded-xl bg-indigo-600 text-white shadow-md shadow-indigo-200 transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-40" aria-label="Send question"><Send size={17} /></button>
      </form>
      <p className="mt-2 text-center text-[11px] text-slate-400">Foxsec can make mistakes. Verify important answers against the source material.</p>
    </div>
  )
}

export default ChatComposer
