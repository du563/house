import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

function pad2 (n) {
  return String(n).padStart(2, '0')
}

export function formatNowForFilename () {
  const d = new Date()
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}_${pad2(d.getHours())}${pad2(d.getMinutes())}${pad2(d.getSeconds())}`
}

/**
 * 将订单数组导出为 Excel（.xlsx）
 * @param {Array<object>} orders
 * @param {object} opts
 * @param {string} opts.filenameBase
 * @param {(status:number)=>string} opts.statusText
 */
export function exportOrdersToExcel (orders, { filenameBase, statusText }) {
  const rows = (orders || []).map((o, idx) => ({
    '序号': idx + 1,
    '订单号': o.orderNo || '',
    '房源名称': o.houseName || '',
    '入住日期': o.checkInDate || '',
    '离店日期': o.checkOutDate || '',
    '入住晚数': o.days != null ? o.days : '',
    '入住人数': o.guests != null ? o.guests : '',
    '总价(元)': o.totalPrice != null ? Number(o.totalPrice) : '',
    '联系人': o.contactName || '',
    '联系电话': o.contactPhone || '',
    '状态': statusText ? statusText(o.status) : (o.status != null ? String(o.status) : ''),
    '创建时间': o.createTime || ''
  }))

  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '订单')

  const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  const blob = new Blob([buf], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const fn = `${filenameBase}_${formatNowForFilename()}.xlsx`
  saveAs(blob, fn)
}

