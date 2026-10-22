import { AnimatePresence, motion } from 'motion/react'
import { FileText, Plus, UploadCloud, X } from 'lucide-react'
import { useRef } from 'react'

function UploadPanel({ files, uploading, onAddFiles, onRemoveFile, onUpload }) {
  const inputRef = useRef(null)
  const openFilePicker = () => inputRef.current?.click()
  const handleDrop = (event) => { event.preventDefault(); onAddFiles([...event.dataTransfer.files]) }

  return (
    <section className="panel p-5">
      <div className="mb-4 flex items-center gap-2"><UploadCloud size={18} className="text-indigo-600" /><h2 className="font-semibold">Add knowledge</h2></div>
      <div onDrop={handleDrop} onDragOver={(event) => event.preventDefault()} onClick={openFilePicker} className="group cursor-pointer rounded-2xl border-2 border-dashed border-indigo-200 bg-indigo-50/50 p-6 text-center transition hover:border-indigo-400 hover:bg-indigo-50">
        <input ref={inputRef} type="file" multiple className="hidden" onChange={(event) => onAddFiles([...event.target.files])} />
        <div className="mx-auto mb-3 grid h-11 w-11 place-items-center rounded-xl bg-white text-indigo-600 shadow-sm transition group-hover:-translate-y-0.5"><Plus size={22} /></div>
        <p className="text-sm font-semibold">Drop files here or browse</p><p className="mt-1 text-xs text-slate-500">PDF, DOCX, TXT and more · up to 25 MB</p>
      </div>
      <AnimatePresence>{files.length > 0 && <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="mt-3 space-y-2 overflow-hidden">
        {files.map((file, index) => <div className="flex items-center gap-2 rounded-lg bg-slate-50 p-2 text-xs" key={`${file.name}-${index}`}><FileText size={15} className="shrink-0 text-indigo-500" /><span className="min-w-0 flex-1 truncate">{file.name}</span><button onClick={() => onRemoveFile(index)} aria-label={`Remove ${file.name}`}><X size={15} /></button></div>)}
        <button onClick={onUpload} disabled={uploading} className="primary-button w-full">{uploading ? 'Indexing documents…' : `Upload ${files.length} file${files.length > 1 ? 's' : ''}`}</button>
      </motion.div>}</AnimatePresence>
    </section>
  )
}

export default UploadPanel
