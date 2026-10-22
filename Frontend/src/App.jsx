import { useDocumentWorkspace } from './hooks/useDocumentWorkspace'
import AppHeader from './components/layout/AppHeader'
import UploadPanel from './components/documents/UploadPanel'
import DocumentLibrary from './components/documents/DocumentLibrary'
import ChatWorkspace from './components/chat/ChatWorkspace'
import ErrorAlert from './components/ui/ErrorAlert'

function App() {
  const workspace = useDocumentWorkspace()

  return (
    <main className="min-h-screen bg-[#f7f8fc] text-slate-900">
      <AppHeader />
      <div className="mx-auto grid max-w-[1600px] gap-5 p-5 lg:grid-cols-[360px_minmax(0,1fr)] lg:p-8">
        <aside className="space-y-5">
          <UploadPanel
            files={workspace.files}
            uploading={workspace.uploading}
            onAddFiles={workspace.addFiles}
            onRemoveFile={workspace.removeFile}
            onUpload={workspace.uploadFiles}
          />
          <DocumentLibrary
            documents={workspace.documents}
            loading={workspace.loading}
            selectedId={workspace.selectedId}
            onSelect={workspace.setSelectedId}
            onDelete={workspace.deleteDocument}
          />
        </aside>

        <ChatWorkspace
          documents={workspace.documents}
          selectedId={workspace.selectedId}
          messages={workspace.messages}
          sending={workspace.sending}
          onSelectDocument={workspace.setSelectedId}
          onAsk={workspace.askQuestion}
        />
      </div>

      <ErrorAlert message={workspace.error} onDismiss={workspace.clearError} />
    </main>
  )
}

export default App
