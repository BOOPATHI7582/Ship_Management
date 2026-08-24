import api from './axios'

export function fetchReportOverview() {
  return api.get('/manager/reports/overview')
}
