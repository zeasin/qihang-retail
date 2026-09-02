import request from '@/utils/request'

export function getUnreadMessages() {
  return request({ url: '/sys/message/unread', method: 'get' })
}

export function countUnreadMessages() {
  return request({ url: '/sys/message/count', method: 'get' })
}

export function markMessageRead(id: number) {
  return request({ url: `/sys/message/read/${id}`, method: 'post' })
}

export function markAllMessagesRead() {
  return request({ url: '/sys/message/read-all', method: 'post' })
}
