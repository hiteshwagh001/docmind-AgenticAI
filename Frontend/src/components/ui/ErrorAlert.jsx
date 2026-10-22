import { AnimatePresence, motion } from 'motion/react'
import { X } from 'lucide-react'

function ErrorAlert({ message, onDismiss }) {
  return <AnimatePresence>{message && <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 12 }} className="fixed bottom-5 right-5 z-30 flex max-w-md items-center gap-3 rounded-xl border border-rose-100 bg-white px-4 py-3 text-sm text-rose-700 shadow-xl"><span className="flex-1">{message}</span><button onClick={onDismiss} aria-label="Dismiss error"><X size={16} /></button></motion.div>}</AnimatePresence>
}

export default ErrorAlert
