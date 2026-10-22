import { useCallback, useEffect, useState } from 'react'
import { foxsecApi } from '../api'

const MAX_FILE_SIZE = 25 * 1024 * 1024
const welcomeMessage = {
  id: 'welcome',
  role: 'assistant',
  text: 'Hello! I’m Foxsec. Upload a document, then ask me to summarize it, find a detail, or explain a concept from it.',
}

export function useDocumentWorkspace() {
  const [documents, setDocuments] = useState([])
  const [selectedId, setSelectedId] = useState('')
  const [messages, setMessages] = useState([welcomeMessage])
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')

  const loadDocuments = useCallback(async () => {
    setLoading(true)
    try {
      setDocuments(await foxsecApi.getDocuments())
    } catch (requestError) {
      setError(`Could not reach the backend. ${requestError.message}`)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadDocuments() }, [loadDocuments])

  const addFiles = (newFiles) => {
    const oversized = newFiles.find((file) => file.size > MAX_FILE_SIZE)
    if (oversized) {
      setError(`“${oversized.name}” is larger than the 25 MB upload limit.`)
      return
    }
    setError('')
    setFiles((current) => [...current, ...newFiles])
  }

  const removeFile = (index) => setFiles((current) => current.filter((_, itemIndex) => itemIndex !== index))

  const uploadFiles = async () => {
    if (!files.length) return
    setUploading(true)
    setError('')
    try {
      await foxsecApi.uploadDocuments(files)
      setFiles([])
      await loadDocuments()
    } catch (requestError) {
      setError(`Upload failed: ${requestError.message}`)
    } finally {
      setUploading(false)
    }
  }

  const deleteDocument = async (id) => {
    if (!window.confirm('Remove this document and its indexed chunks?')) return
    try {
      await foxsecApi.deleteDocument(id)
      setDocuments((current) => current.filter((document) => document.id !== id))
      if (selectedId === id) setSelectedId('')
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  const askQuestion = async (question) => {
    if (!question.trim() || sending) return
    const userMessage = { id: crypto.randomUUID(), role: 'user', text: question.trim() }
    setMessages((current) => [...current, userMessage])
    setSending(true)
    setError('')

    try {
      const response = await foxsecApi.ask(question, selectedId)
      setMessages((current) => [...current, {
        id: crypto.randomUUID(),
        role: 'assistant',
        text: response.answer || 'I could not generate an answer for that question.',
        citations: response.citations || [],
      }])
    } catch (requestError) {
      setError(requestError.message)
      setMessages((current) => [...current, {
        id: crypto.randomUUID(),
        role: 'assistant',
        text: 'I wasn’t able to answer that just now. Please make sure the backend and OpenAI configuration are running, then try again.',
      }])
    } finally {
      setSending(false)
    }
  }

  return {
    documents, selectedId, messages, files, loading, uploading, sending, error,
    setSelectedId, addFiles, removeFile, uploadFiles, deleteDocument, askQuestion,
    clearError: () => setError(''),
  }
}
