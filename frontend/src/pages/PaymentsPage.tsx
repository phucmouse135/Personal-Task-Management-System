import { useState } from 'react';
import { usePayments } from '@/hooks/usePayments';
import { Payment, PaymentStatus } from '@/types';

export const PaymentsPage = () => {
  const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);

  const { payments, loading, error } = usePayments({
    page: currentPage,
    size: 20,
    status: statusFilter !== 'all' ? statusFilter : undefined,
  });

  const getStatusColor = (status: PaymentStatus) => {
    const colors = {
      [PaymentStatus.COMPLETED]: 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400',
      [PaymentStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400',
      [PaymentStatus.FAILED]: 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400',
    };
    return colors[status] || colors[PaymentStatus.PENDING];
  };

  const filteredPayments = payments.filter(payment => {
    const searchLower = searchQuery.toLowerCase();
    const matchesSearch = 
      payment.id.toString().includes(searchLower) ||
      payment.amount.toString().includes(searchLower) ||
      (payment.vnpTransactionNo && payment.vnpTransactionNo.toLowerCase().includes(searchLower)) ||
      (payment.project?.name && payment.project.name.toLowerCase().includes(searchLower));
    return matchesSearch;
  });

  const totalAmount = filteredPayments.reduce((sum, p) => sum + (p.status === PaymentStatus.COMPLETED ? p.amount : 0), 0);
  const pendingAmount = filteredPayments.reduce((sum, p) => sum + (p.status === PaymentStatus.PENDING ? p.amount : 0), 0);
  const successRate = payments.length > 0 ? (payments.filter(p => p.status === PaymentStatus.COMPLETED).length / payments.length) * 100 : 0;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Payments</h1>
          <p className="text-gray-600 dark:text-gray-400">Manage your payment transactions (VNPAY Integration)</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
            <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">Total Completed</div>
            <div className="text-2xl font-bold text-gray-900 dark:text-white">{totalAmount.toLocaleString()} VND</div>
          </div>
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
            <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">Pending</div>
            <div className="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{pendingAmount.toLocaleString()} VND</div>
          </div>
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
            <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">Total Transactions</div>
            <div className="text-2xl font-bold text-gray-900 dark:text-white">{filteredPayments.length}</div>
          </div>
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg border border-gray-200 dark:border-gray-700">
            <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">Success Rate</div>
            <div className="text-2xl font-bold text-green-600 dark:text-green-400">
              {successRate.toFixed(0)}%
            </div>
          </div>
        </div>

        {/* Filters and Actions */}
        <div className="bg-white dark:bg-gray-800 p-4 rounded-lg border border-gray-200 dark:border-gray-700 mb-6">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="Search by ID, amount, or transaction number..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
            >
              <option value="all">All Status</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="FAILED">Failed</option>
            </select>
          </div>
        </div>

        {loading && <div className="text-center py-12 text-gray-600 dark:text-gray-400">Loading payments...</div>}
        {error && <div className="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 p-4 rounded mb-4">{error}</div>}

        {!loading && !error && filteredPayments.length === 0 && (
          <div className="text-center py-12 bg-white dark:bg-gray-800 rounded-lg">
            <p className="text-gray-500 dark:text-gray-400">No payments found</p>
          </div>
        )}

        {/* Payments Table */}
        {!loading && !error && filteredPayments.length > 0 && (
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Payment ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Project
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Amount
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      VNPAY Transaction
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                  {filteredPayments.map((payment) => (
                    <tr key={payment.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          #{payment.id}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-gray-900 dark:text-white">
                          {payment.project?.name || 'N/A'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          {payment.amount.toLocaleString()} VND
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(payment.status)}`}>
                          {payment.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-600 dark:text-gray-400">
                          {payment.vnpTransactionNo || 'N/A'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-400">
                        {new Date(payment.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <button
                          onClick={() => setSelectedPayment(payment)}
                          className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                        >
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Payment Details Modal */}
        {selectedPayment && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full p-6">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">Payment Details</h2>
              <div className="space-y-3">
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Payment ID</label>
                  <p className="text-gray-900 dark:text-white font-medium">#{selectedPayment.id}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Project</label>
                  <p className="text-gray-900 dark:text-white">{selectedPayment.project?.name || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Amount</label>
                  <p className="text-gray-900 dark:text-white font-medium">
                    {selectedPayment.amount.toLocaleString()} VND
                  </p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">VNPAY Transaction ID</label>
                  <p className="text-gray-900 dark:text-white">{selectedPayment.vnpTransactionNo || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Bank Code</label>
                  <p className="text-gray-900 dark:text-white">{selectedPayment.vnpBankCode || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Pay Date</label>
                  <p className="text-gray-900 dark:text-white">{selectedPayment.vnpPayDate || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Status</label>
                  <div className="mt-1">
                    <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(selectedPayment.status)}`}>
                      {selectedPayment.status}
                    </span>
                  </div>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Created At</label>
                  <p className="text-gray-900 dark:text-white">{new Date(selectedPayment.createdAt).toLocaleString()}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-600 dark:text-gray-400">Updated At</label>
                  <p className="text-gray-900 dark:text-white">{new Date(selectedPayment.updatedAt).toLocaleString()}</p>
                </div>
              </div>
              <div className="flex gap-3 mt-6">
                <button
                  onClick={() => setSelectedPayment(null)}
                  className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
