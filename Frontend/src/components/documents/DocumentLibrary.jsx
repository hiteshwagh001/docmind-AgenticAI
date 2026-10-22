import { FileText, FolderOpen, Trash2 } from 'lucide-react'
import { formatFileSize } from '../../utils/formatters'

const statusColors = { INDEXED: 'bg-emerald-500', FAILED: 'bg-rose-500' }

function DocumentLibrary({ documents, loading, selectedId, onSelect, onDelete }) {
  return (
    <section className="panel overflow-hidden">
      <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4"><div className="flex items-center gap-2"><FolderOpen size={18} className="text-indigo-600" /><h2 className="font-semibold">Your documents</h2></div><span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">{documents.length}</span></div>
      <div className="max-h-[390px] overflow-y-auto p-2">
        {loading && <p className="p-4 text-center text-sm text-slate-400">Loading library…</p>}
        {!loading && documents.length === 0 && <div className="p-6 text-center"><FileText className="mx-auto mb-2 text-slate-300" /><p className="text-sm text-slate-500">Your library is empty.</p></div>}
        {documents.map((document) => <button key={document.id} onClick={() => onSelect(document.id)} className={`group flex w-full items-center gap-3 rounded-xl p-3 text-left transition ${selectedId === document.id ? 'bg-indigo-50 ring-1 ring-indigo-200' : 'hover:bg-slate-50'}`}>
          <div className={`grid h-9 w-9 shrink-0 place-items-center rounded-lg ${selectedId === document.id ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-500'}`}><FileText size={17} /></div>
          <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{document.filename}</p><p className="mt-0.5 text-xs text-slate-500">{formatFileSize(document.fileSize)} · {document.totalChunks ?? 0} chunks</p></div>
          <span title={document.status} className={`h-2 w-2 rounded-full ${statusColors[document.status] ?? 'bg-amber-400'}`} />
          <span role="button" tabIndex={0} onClick={(event) => { event.stopPropagation(); onDelete(document.id) }} className="rounded-md p-1 text-slate-300 opacity-0 transition hover:bg-rose-50 hover:text-rose-500 group-hover:opacity-100"><Trash2 size={15} /></span>
        </button>)}
      </div>
    </section>
  )
}

export default DocumentLibrary
