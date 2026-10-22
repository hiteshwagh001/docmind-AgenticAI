import { Sparkles } from 'lucide-react'

function AppHeader() {
  return (
    <header className="sticky top-0 z-20 border-b border-slate-200/80 bg-white/80 backdrop-blur-xl">
      <div className="mx-auto flex h-17 max-w-[1600px] items-center justify-between px-5 lg:px-8">
        <div className="flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br from-indigo-600 to-violet-500 shadow-lg shadow-indigo-200"><Sparkles size={19} className="text-white" /></div>
          <div><p className="text-lg font-bold tracking-tight">Foxsec</p><p className="text-xs text-slate-500">Your intelligent document workspace</p></div>
        </div>
        <div className="hidden items-center gap-2 rounded-full border border-emerald-100 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-700 sm:flex"><span className="h-2 w-2 rounded-full bg-emerald-500" /> System online</div>
      </div>
    </header>
  )
}

export default AppHeader
