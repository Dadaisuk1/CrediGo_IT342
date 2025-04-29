import React, { useEffect, useState } from 'react';
import {
  getKYCRequests,
  approveKYCRequest,
  rejectKYCRequest,
  deleteKYCRequest
} from '../services/api';

const AdminKYC = () => {
  const [kycRequests, setKycRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null); // id of row being acted upon
  const [actionError, setActionError] = useState(null);

  const fetchKYC = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await getKYCRequests();
      setKycRequests(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch KYC requests');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKYC();
  }, []);

  const handleApprove = async (id) => {
    setActionLoading(id);
    setActionError(null);
    try {
      await approveKYCRequest(id);
      await fetchKYC();
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Failed to approve KYC');
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (id) => {
    setActionLoading(id);
    setActionError(null);
    try {
      await rejectKYCRequest(id);
      await fetchKYC();
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Failed to reject KYC');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (id) => {
    setActionLoading(id);
    setActionError(null);
    try {
      await deleteKYCRequest(id);
      await fetchKYC();
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Failed to delete KYC request');
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="p-4">
      <h2 className="text-2xl font-bold mb-4">KYC Approvals</h2>
      {loading ? (
        <div className="text-gray-500">Loading KYC requests...</div>
      ) : error ? (
        <div className="text-red-500">{error}</div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border border-gray-200 rounded-lg">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-4 py-2">ID</th>
                <th className="px-4 py-2">User</th>
                <th className="px-4 py-2">Document Type</th>
                <th className="px-4 py-2">Document Number</th>
                <th className="px-4 py-2">Status</th>
                <th className="px-4 py-2">Submitted At</th>
                <th className="px-4 py-2">Reviewed At</th>
                <th className="px-4 py-2">Admin Comment</th>
                <th className="px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {kycRequests.length === 0 ? (
                <tr>
                  <td colSpan={9} className="text-center text-gray-500 py-4">No KYC requests found.</td>
                </tr>
              ) : (
                kycRequests.map((req) => (
                  <tr key={req.id} className="border-t">
                    <td className="px-4 py-2">{req.id}</td>
                    <td className="px-4 py-2">{req.user?.username || req.user?.email || req.user?.id}</td>
                    <td className="px-4 py-2">{req.documentType}</td>
                    <td className="px-4 py-2">{req.documentNumber}</td>
                    <td className="px-4 py-2">
                      <span className={
                        req.status === 'APPROVED' ? 'text-green-600' : req.status === 'REJECTED' ? 'text-red-600' : 'text-yellow-600'}>
                        {req.status}
                      </span>
                    </td>
                    <td className="px-4 py-2">{req.submittedAt ? new Date(req.submittedAt).toLocaleString() : '-'}</td>
                    <td className="px-4 py-2">{req.reviewedAt ? new Date(req.reviewedAt).toLocaleString() : '-'}</td>
                    <td className="px-4 py-2">{req.adminComment || '-'}</td>
                    <td className="px-4 py-2 space-x-2">
                      {req.status === 'PENDING' && (
                        <>
                          <button
                            className="bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded text-xs"
                            disabled={actionLoading === req.id}
                            onClick={() => handleApprove(req.id)}
                          >
                            {actionLoading === req.id ? 'Approving...' : 'Approve'}
                          </button>
                          <button
                            className="bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded text-xs"
                            disabled={actionLoading === req.id}
                            onClick={() => handleReject(req.id)}
                          >
                            {actionLoading === req.id ? 'Rejecting...' : 'Reject'}
                          </button>
                        </>
                      )}
                      <button
                        className="bg-gray-400 hover:bg-gray-500 text-white px-2 py-1 rounded text-xs"
                        disabled={actionLoading === req.id}
                        onClick={() => handleDelete(req.id)}
                      >
                        {actionLoading === req.id ? 'Deleting...' : 'Delete'}
                      </button>
                      {actionError && actionLoading === req.id && (
                        <div className="text-xs text-red-500 mt-1">{actionError}</div>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminKYC;
