import { motion } from 'motion/react'
import { Bot } from 'lucide-react'

function ChatMessages({ messages, sending, endRef }) {
  return (
    <div className="flex-1 space-y-6 overflow-y-auto bg-gradient-to-b from-white to-slate-50/60 px-5 py-7 sm:px-9">
      {messages.map((message) => <motion.article initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} key={message.id} className={`flex gap-3 ${message.role === 'user' ? 'justify-end' : ''}`}>
        {message.role === 'assistant' && <div className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-gradient-to-br from-indigo-600 to-violet-500 text-white"><Bot size={18} /></div>}
        <div className={`max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-6 shadow-sm ${message.role === 'user' ? 'rounded-tr-sm bg-indigo-600 text-white' : 'rounded-tl-sm border border-slate-100 bg-white text-slate-700'}`}>
          <p className="whitespace-pre-wrap">{message.text}</p>
          {message.citations?.length > 0 && <div className="mt-3 border-t border-slate-100 pt-2"><p className="mb-1 text-xs font-semibold text-slate-400">SOURCES</p>{message.citations.slice(0, 3).map((citation, index) => <p className="text-xs text-indigo-600" key={index}>{citation.fileName || 'Document'}{citation.pageNumber ? ` · Page ${citation.pageNumber}` : ''}</p>)}</div>}
        </div>
        {message.role === 'user' && <div className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-slate-200 text-xs font-bold text-slate-600">You</div>}
      </motion.article>)}
      {sending && <div className="flex gap-3"><div className="grid h-9 w-9 place-items-center rounded-xl bg-indigo-600 text-white"><Bot size={18} /></div><div className="typing-dots rounded-2xl rounded-tl-sm border border-slate-100 bg-white px-4 py-3"><i /><i /><i /></div></div>}
      <div ref={endRef} />
    </div>
  )
}

export default ChatMessages
