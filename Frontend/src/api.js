const API_URL = import.meta.env.VITE_API_URL ?? ''

async function request(path, options = {}) {
  const response = await fetch(`${API_URL}${path}`, options)
  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text().catch(() => '')

  if (!response.ok || payload?.success === false) {
    const message = typeof payload === 'object' ? payload?.message : payload
    throw new Error(message || `Request failed (${response.status} ${response.statusText}).`)
  }
  return payload?.data
}

export const docmindApi = {
  getDocuments: () => request('/api/v1/documents'),
  deleteDocument: (id) => request(`/api/v1/documents/${id}`, { method: 'DELETE' }),
  uploadDocuments: (files) => {
    const body = new FormData()
    if (files.length === 1) {
      body.append('file', files[0])
      return request('/api/v1/documents/upload', { method: 'POST', body })
    }
    files.forEach((file) => body.append('files', file))
    return request('/api/v1/documents/upload-multiple', { method: 'POST', body })
  },
  ask: (question, documentId) => request('/chat/query', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ question, documentId: documentId || null }) }),
}
