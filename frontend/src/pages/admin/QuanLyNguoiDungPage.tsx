import { useState, useEffect, useCallback } from 'react';
import { Search, UserCheck, UserX, Shield, Plus, X } from 'lucide-react';
import { adminApi } from '../../services/api';
import type { NguoiDungAdmin } from '../../services/api';
import { formatDate } from '../../utils/format';
import toast from 'react-hot-toast';
import LoadingSpinner from '../../components/LoadingSpinner';
import { useAuthStore } from '../../stores/authStore';

export default function QuanLyNguoiDungPage() {
  const [danhSach, setDanhSach] = useState<NguoiDungAdmin[]>([]);
  const [loading, setLoading] = useState(true);
  const [tuKhoa, setTuKhoa] = useState('');
  const [trang, setTrang] = useState(0);
  const [tongTrang, setTongTrang] = useState(0);
  const [tabVaiTro, setTabVaiTro] = useState<string>('');
  const [showCreate, setShowCreate] = useState(false);
  const { isAdmin } = useAuthStore();

  const load = useCallback(() => {
    setLoading(true);
    adminApi.danhSachNguoiDung({ tuKhoa: tuKhoa || undefined, trang, tenVaiTro: tabVaiTro || undefined })
      .then(r => {
        setDanhSach(r.data.duLieu?.nguoiDung || []);
        setTongTrang(r.data.duLieu?.tongTrang || 0);
      })
      .finally(() => setLoading(false));
  }, [tuKhoa, trang, tabVaiTro]);

  useEffect(() => { load(); }, [load]);

  const toggleActive = async (u: NguoiDungAdmin) => {
    try {
      await adminApi.doiTrangThaiNguoiDung(u.id, !u.dangHoatDong);
      toast.success(u.dangHoatDong ? 'Đã khóa tài khoản' : 'Đã mở khóa');
      load();
    } catch {
      toast.error('Có lỗi xảy ra');
    }
  };

  const doiVaiTro = async (id: number, vaiTroId: number) => {
    try {
      await adminApi.doiVaiTroNguoiDung(id, vaiTroId);
      toast.success('Đã đổi vai trò');
      load();
    } catch {
      toast.error('Có lỗi xảy ra');
    }
  };

  const vaiTroLabel = (vt: NguoiDungAdmin['vaiTro']) => {
    const ten = vt?.tenVaiTro || '';
    if (ten === 'ADMIN') return <span className="badge badge-red text-xs">Admin</span>;
    if (ten === 'NHAN_VIEN') return <span className="badge badge-blue text-xs">Nhân viên</span>;
    if (ten === 'KHACH_HANG') return <span className="badge badge-gray text-xs">Khách hàng</span>;
    return <span className="badge badge-gray text-xs">{ten || 'Không xác định'}</span>;
  };

  const getTenVaiTro = (u: NguoiDungAdmin) => u.vaiTro?.tenVaiTro || '';


  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-gray-900">Quản lý người dùng</h2>
        {isAdmin() && (
          <button onClick={() => setShowCreate(true)} className="btn-primary text-sm flex items-center gap-2">
            <Plus className="w-4 h-4" /> Tạo nhân viên
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-100 p-4 mb-5 flex flex-col sm:flex-row sm:items-center gap-3">
        <div className="relative max-w-xs">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input className="input-field pl-9 text-sm" placeholder="Tìm tên, email, SĐT..."
            value={tuKhoa} onChange={e => { setTuKhoa(e.target.value); setTrang(0); }} />
        </div>
        <div className="flex items-center gap-1 ml-auto">
          {[
            { label: 'Tất cả', value: '' },
            { label: 'Khách hàng', value: 'KHACH_HANG' },
            { label: 'Nhân viên', value: 'NHAN_VIEN' },
          ].map(tab => (
            <button
              key={tab.value}
              onClick={() => { setTabVaiTro(tab.value); setTrang(0); }}
              className={`px-3 py-1.5 text-sm rounded-lg font-medium transition-colors ${
                tabVaiTro === tab.value
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {loading ? <LoadingSpinner /> : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Người dùng</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Liên hệ</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Vai trò</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Ngày tạo</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Trạng thái</th>
                {isAdmin() && <th className="px-4 py-3"></th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {danhSach.map(u => (
                <tr key={u.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0">
                        <span className="text-indigo-600 font-semibold text-sm">{u.hoTen?.[0] || '?'}</span>
                      </div>
                      <div>
                        <span className="font-medium text-gray-900">{u.hoTen}</span>
                        <p className="text-xs text-gray-400">@{u.tenDangNhap}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <p className="text-gray-700">{u.email}</p>
                    <p className="text-gray-400 text-xs">{u.soDienThoai}</p>
                  </td>
                  <td className="px-4 py-3">{vaiTroLabel(u.vaiTro)}</td>
                  <td className="px-4 py-3 text-gray-500">{formatDate(u.thoiGianTao)}</td>
                  <td className="px-4 py-3">
                    {u.dangHoatDong ? (
                      <span className="badge badge-green text-xs">Hoạt động</span>
                    ) : (
                      <span className="badge badge-red text-xs">Bị khóa</span>
                    )}
                  </td>
                  {isAdmin() && (
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <button onClick={() => toggleActive(u)}
                          title={u.dangHoatDong ? 'Khóa tài khoản' : 'Mở khóa'}
                          className="p-1.5 text-gray-400 hover:text-indigo-600 transition-colors">
                          {u.dangHoatDong ? <UserX className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                        </button>
                        {getTenVaiTro(u) === 'KHACH_HANG' && (
                          <button onClick={() => doiVaiTro(u.id, 2)}
                            title="Thăng lên Nhân viên"
                            className="p-1.5 text-gray-400 hover:text-blue-600 transition-colors">
                            <Shield className="w-4 h-4" />
                          </button>
                        )}
                        {getTenVaiTro(u) === 'NHAN_VIEN' && (
                          <button onClick={() => doiVaiTro(u.id, 3)}
                            title="Hạ xuống Khách hàng"
                            className="text-xs px-2 py-1 text-red-500 hover:bg-red-50 rounded transition-colors">
                            Hạ cấp
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))}
              {danhSach.length === 0 && (
                <tr><td colSpan={6} className="text-center py-10 text-gray-400">Không có người dùng</td></tr>
              )}
            </tbody>
          </table>
          {tongTrang > 1 && (
            <div className="flex justify-center gap-2 p-4 border-t border-gray-100">
              <button disabled={trang === 0} onClick={() => setTrang(t => t - 1)} className="btn-secondary text-sm">Trước</button>
              <span className="px-3 py-2 text-sm text-gray-600">{trang + 1}/{tongTrang}</span>
              <button disabled={trang >= tongTrang - 1} onClick={() => setTrang(t => t + 1)} className="btn-secondary text-sm">Sau</button>
            </div>
          )}
        </div>
      )}

      {showCreate && (
        <CreateStaffModal
          onClose={() => setShowCreate(false)}
          onSaved={() => { setShowCreate(false); setTabVaiTro('NHAN_VIEN'); setTrang(0); load(); }}
        />
      )}
    </div>
  );
}

function CreateStaffModal({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
  const [form, setForm] = useState({ tenDangNhap: '', matKhau: '', hoTen: '', email: '', soDienThoai: '' });
  const [saving, setSaving] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!/^[a-zA-Z0-9_]{4,50}$/.test(form.tenDangNhap)) {
      toast.error('Tên đăng nhập phải từ 4-50 ký tự, chỉ gồm chữ, số và dấu _');
      return;
    }
    if (form.matKhau.length < 6 || form.matKhau.length > 100) {
      toast.error('Mật khẩu phải từ 6-100 ký tự');
      return;
    }
    setSaving(true);
    try {
      await adminApi.taoNhanVien({ ...form, soDienThoai: form.soDienThoai || undefined });
      toast.success('Tạo nhân viên thành công');
      onSaved();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { thongBao?: string } } })?.response?.data?.thongBao;
      toast.error(msg || 'Không thể tạo nhân viên');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="create-staff-title">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h3 id="create-staff-title" className="font-bold text-gray-900">Tạo tài khoản nhân viên</h3>
          <button onClick={onClose} className="p-1 text-gray-400 hover:text-gray-700" aria-label="Đóng">
            <X className="w-5 h-5" />
          </button>
        </div>
        <form onSubmit={submit} className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="sm:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">Họ tên *</label>
            <input className="input-field" required maxLength={150} value={form.hoTen}
              onChange={e => setForm({ ...form, hoTen: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tên đăng nhập *</label>
            <input className="input-field" required minLength={4} maxLength={50} autoComplete="off" value={form.tenDangNhap}
              onChange={e => setForm({ ...form, tenDangNhap: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mật khẩu *</label>
            <input type="password" className="input-field" required minLength={6} maxLength={100} autoComplete="new-password" value={form.matKhau}
              onChange={e => setForm({ ...form, matKhau: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
            <input type="email" className="input-field" required value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })} />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
            <input type="tel" className="input-field" pattern="0[0-9]{9}" value={form.soDienThoai}
              onChange={e => setForm({ ...form, soDienThoai: e.target.value })} />
          </div>
          <div className="sm:col-span-2 flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary">Hủy</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving ? 'Đang tạo...' : 'Tạo nhân viên'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
