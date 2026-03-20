// 时间格式化工具
export const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  // 如果是字符串，替换T为空格，并去掉毫秒部分
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  // 如果是Date对象，格式化为字符串
  if (dateTime instanceof Date) {
    return dateTime.toISOString().replace('T', ' ').substring(0, 19)
  }
  return dateTime
}





