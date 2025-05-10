import { useEffect, useState } from 'react';
import { FaCheck, FaEye, FaFilter, FaSearch, FaTimes, FaUserCheck, FaUserClock } from 'react-icons/fa';
import {
  approveKYCRequest,
  deleteKYCRequest,
  getKYCRequests,
  rejectKYCRequest
} from '../services/api';

const AdminKYC = () => {
  const [kycApplications, setKycApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('pending');
  const [searchTerm, setSearchTerm] = useState('');
  const [viewApplication, setViewApplication] = useState(null);
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(null); // id of row being acted upon
  const [actionError, setActionError] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const fetchKYC = async () => {
    setLoading(true);
    setActionError(null);
    try {
      const res = await getKYCRequests();
      setKycApplications(res.data);
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Failed to fetch KYC requests');
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

  // Merged handleReject function that works with both API and UI updates
  const handleReject = async (id, reason = '') => {
    setActionLoading(id);
    setActionError(null);
    setShowVerificationModal(false);

    try {
      // If reason is provided, handle as local UI update
      if (reason) {
        setKycApplications(prevApplications =>
          prevApplications.map(app =>
            app.id === id ? {
              ...app,
              status: 'rejected',
              rejectedDate: new Date().toISOString().split('T')[0],
              rejectionReason: reason
            } : app
          )
        );
      } else {
        // Otherwise, call the API
        await rejectKYCRequest(id);
        await fetchKYC();
      }
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

  const handleVerify = (id) => {
    setKycApplications(prevApplications =>
      prevApplications.map(app =>
        app.id === id ? { ...app, status: 'verified', verifiedDate: new Date().toISOString().split('T')[0] } : app
      )
    );
    setShowVerificationModal(false);
  };

  const filteredApplications = kycApplications.filter(app => {
    return (
      (filter === 'all' || app.status === filter) &&
      (searchTerm === '' || app.user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
       app.user.email.toLowerCase().includes(searchTerm.toLowerCase()))
    );
  });

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'verified':
        return 'bg-green-100 text-green-800';
      case 'rejected':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#eebbc3]"></div>
    </div>;
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 p-6">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-[#232946]">KYC Verification</h1>
            <p className="text-gray-500 mt-1">Manage user verification documents</p>
          </div>
          <div className="mt-4 md:mt-0 flex flex-wrap gap-2">
            <div className="relative">
              <input
                type="text"
                placeholder="Search users..."
                className="px-4 py-2 pl-10 pr-4 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            </div>
            <div className="relative">
              <select
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                className="px-4 py-2 pl-10 border border-gray-200 rounded-lg appearance-none focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
              >
                <option value="all">All Applications</option>
                <option value="pending">Pending</option>
                <option value="verified">Verified</option>
                <option value="rejected">Rejected</option>
              </select>
              <FaFilter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            </div>
          </div>
        </div>
      </div>

      {/* KYC Applications List */}
      <div className="bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-3">User</th>
                <th className="px-6 py-3">Submitted Date</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredApplications.length > 0 ? (
                filteredApplications.map((application) => (
                  <tr key={application.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <img
                          src={application.user.avatar}
                          alt={application.user.name}
                          className="w-10 h-10 rounded-full border border-gray-200"
                        />
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{application.user.name}</div>
                          <div className="text-sm text-gray-500">{application.user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">{application.submittedDate}</td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeClass(application.status)}`}>
                        {application.status === 'pending' && <FaUserClock className="mr-1" />}
                        {application.status === 'verified' && <FaUserCheck className="mr-1" />}
                        {application.status === 'rejected' && <FaTimes className="mr-1" />}
                        {application.status.charAt(0).toUpperCase() + application.status.slice(1)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium">
                      <button
                        onClick={() => {
                          setViewApplication(application);
                          setShowVerificationModal(true);
                        }}
                        className="text-[#eebbc3] hover:text-[#d39ba4] mr-3"
                      >
                        <FaEye size={18} />
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" className="px-6 py-12 text-center text-gray-500">
                    No KYC applications match your criteria
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Verification Modal */}
      {showVerificationModal && viewApplication && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-lg font-semibold text-[#232946]">
                KYC Application - {viewApplication.user.name}
              </h3>
              <button
                onClick={() => setShowVerificationModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <FaTimes />
              </button>
            </div>

            <div className="p-6 space-y-6">
              {/* User Info */}
              <div className="bg-[#f9f9f1] p-4 rounded-lg">
                <h4 className="font-semibold mb-3 text-[#232946]">User Information</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Full Name</p>
                    <p className="text-base">{viewApplication.information.fullName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Birth Date</p>
                    <p className="text-base">{viewApplication.information.birthDate}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Address</p>
                    <p className="text-base">{viewApplication.information.address}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Phone Number</p>
                    <p className="text-base">{viewApplication.information.phoneNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Nationality</p>
                    <p className="text-base">{viewApplication.information.nationality}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Application Date</p>
                    <p className="text-base">{viewApplication.submittedDate}</p>
                  </div>
                </div>
              </div>

              {/* Documents */}
              <div>
                <h4 className="font-semibold mb-3 text-[#232946]">Verification Documents</h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {viewApplication.documents.map((doc, index) => (
                    <div key={index} className="border border-gray-200 rounded-lg overflow-hidden">
                      <div className="aspect-w-3 aspect-h-2">
                        <img
                          src={doc.url}
                          alt={doc.type}
                          className="w-full h-48 object-cover"
                        />
                      </div>
                      <div className="p-3 bg-white">
                        <p className="text-sm font-medium text-gray-900 capitalize">
                          {doc.type.replace('_', ' ')}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Notes Section */}
              <div className="border border-gray-200 rounded-lg p-4">
                <h4 className="font-semibold mb-2 text-[#232946]">Notes</h4>
                <textarea
                  className="w-full p-2 border border-gray-200 rounded resize-none focus:outline-none focus:ring-2 focus:ring-[#eebbc3]"
                  rows="3"
                  placeholder="Add verification notes here..."
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                ></textarea>
              </div>

              {/* Actions */}
              {viewApplication.status === 'pending' && (
                <div className="flex justify-end space-x-4 border-t border-gray-200 pt-4">
                  <button
                    onClick={() => handleReject(viewApplication.id, rejectionReason || "Documents don't match application details")}
                    className="px-4 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-50 transition-colors"
                  >
                    <FaTimes className="inline mr-2" />
                    Reject
                  </button>
                  <button
                    onClick={() => handleVerify(viewApplication.id)}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                  >
                    <FaCheck className="inline mr-2" />
                    Approve
                  </button>
                </div>
              )}

              {viewApplication.status === 'rejected' && (
                <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
                  <p className="text-sm text-red-700 font-medium">Rejection Reason:</p>
                  <p className="text-sm text-red-600">{viewApplication.rejectionReason}</p>
                  <p className="text-xs text-red-500 mt-1">Rejected on {viewApplication.rejectedDate}</p>
                </div>
              )}

              {viewApplication.status === 'verified' && (
                <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded">
                  <p className="text-sm text-green-700">
                    <FaCheck className="inline mr-1" />
                    Verified on {viewApplication.verifiedDate}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Pending Verifications</p>
              <h3 className="text-2xl font-bold text-[#232946] mt-1">
                {kycApplications.filter(app => app.status === 'pending').length}
              </h3>
            </div>
            <div className="p-3 rounded-full bg-yellow-100 text-yellow-800">
              <FaUserClock size={20} />
            </div>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Verified Users</p>
              <h3 className="text-2xl font-bold text-[#232946] mt-1">
                {kycApplications.filter(app => app.status === 'verified').length}
              </h3>
            </div>
            <div className="p-3 rounded-full bg-green-100 text-green-800">
              <FaUserCheck size={20} />
            </div>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-md border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm">Rejected Applications</p>
              <h3 className="text-2xl font-bold text-[#232946] mt-1">
                {kycApplications.filter(app => app.status === 'rejected').length}
              </h3>
            </div>
            <div className="p-3 rounded-full bg-red-100 text-red-800">
              <FaTimes size={20} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminKYC;
