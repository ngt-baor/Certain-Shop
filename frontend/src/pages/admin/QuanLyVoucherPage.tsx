import { useCallback, useEffect, useState } from 'react';
import { Edit2, Plus, Trash2, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { voucherApi } from '../../services/api';
import type { Voucher } from '../../services/api';
import { formatCurrency, formatDate } from '../../utils/format';
import LoadingSpinner from '../../components/LoadingSpinner';

type VoucherForm = {
  maVoucher: string;
  moTa: string;
  ngayBatDau: string;
  ngayKetThuc: string;
  giaTriToiThieu: string;
  giaTriGiamToiDa: string;
  loaiGiam: 'PERCENT' | 'FIXED';
  giaTriGiam: string;
  soLuongToiDa: string;
  trangThai: boolean;
};

const localDateTime = (date = new Date()) => {
  const offset = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
};

const emptyForm = (): VoucherForm => {
  const end = new Date();
  end.setMonth(end.getMonth() + 1);
  return {
    maVoucher: '', moTa: '', ngayBatDau: localDateTime(), ngayKetThuc: localDateTime(end),
    giaTriToiThieu: '0', giaTriGiamToiDa: '0', loaiGiam: 'PERCENT', giaTriGiam: '',
    soLuongToiDa: '', trangThai: true,
  };
};

const toForm = (voucher: Voucher): VoucherForm => ({
  maVoucher: voucher.maVoucher,
  moTa: voucher.moTa || '',
  ngayBatDau: voucher.ngayBatDau.slice(0, 16),
  ngayKetThuc: voucher.ngayKetThuc.slice(0, 16),
  giaTriToiThieu: String(voucher.giaTriToiThieu ?? 0),
  giaTriGiamToiDa: String(voucher.giaTriGiamToiDa ?? 0),
  loaiGiam: voucher.loaiGiam as 'PERCENT' | 'FIXED',
  giaTriGiam: String(voucher.giaTriGiam),
  soLuongToiDa: voucher.soLuongToiDa == null ? '' : String(voucher.soLuongToiDa),
  trangThai: voucher.trangThai,
});

export default function QuanLyVoucherPage() {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState<Voucher | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<VoucherForm>(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    voucherApi.danhSachTatCa()
      .then(r => setVouchers(r.data.duLieu || []))
      .catch(() => toast.error('Không thể tải danh sách voucher'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => { setEditing(null); setForm(emptyForm()); setShowForm(true); };
  const openEdit = (voucher: Voucher) => { setEditing(voucher); setForm(toForm(voucher)); setShowForm(true); };

  const save = async (e: React.FormEvent) => {
    e.preventDefault();
    if (new Date(form.ngayBatDau) >= new Date(form.ngayKetThuc)) {
      toast.error('Ngày kết thúc phải sau ngày bắt đầu');
      return;
    }
    const discount = Number(form.giaTriGiam);
    if (discount <= 0 || (form.loaiGiam === 'PERCENT' && discount > 100)) {
      toast.error(form.loaiGiam === 'PERCENT' ? 'Phần trăm giảm phải từ 1 đến 100' : 'Giá trị giảm phải lớn hơn 0');
      return;
    }
    const payload = {
      maVoucher: form.maVoucher.trim().toUpperCase(),
      moTa: form.moTa.trim(),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      giaTriToiThieu: Number(form.giaTriToiThieu || 0),
      giaTriGiamToiDa: Number(form.giaTriGiamToiDa || (form.loaiGiam === 'FIXED' ? discount : 0)),
      loaiGiam: form.loaiGiam,
      giaTriGiam: discount,
      soLuongToiDa: form.soLuongToiDa ? Number(form.soLuongToiDa) : null,
      trangThai: form.trangThai,
    };
    setSaving(true);
    try {
      if (editing) await voucherApi.capNhatVoucher(editing.id, payload);
      else await voucherApi.taoVoucher(payload);
      toast.success(editing ? 'Đã cập nhật voucher' : 'Đã tạo voucher');
      setShowForm(false);
      load();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { thongBao?: string } } })?.response?.data?.thongBao;
      toast.error(msg || 'Không thể lưu voucher');
    } finally {
      setSaving(false);
    }
  };

  const remove = async (voucher: Voucher) => {
    if (!confirm(`Ngừng sử dụng voucher ${voucher.maVoucher}?`)) return;
    try {
      await voucherApi.xoaVoucher(voucher.id);
      toast.success('Đã ngừng voucher');
      load();
    } catch {
      toast.error('Không thể ngừng voucher');
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900">Quản lý voucher</h2>
          <p className="text-sm text-gray-500 mt-1">Tạo và kiểm soát mã giảm giá sử dụng tại checkout và quầy.</p>
        </div>
        <button onClick={openCreate} className="btn-primary text-sm flex items-center gap-2">
          <Plus className="w-4 h-4" /> Thêm voucher
        </button>
      </div>

      {loading ? <LoadingSpinner /> : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-x-auto">
          <table className="w-full text-sm min-w-[900px]">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Mã voucher', 'Giảm', 'Điều kiện', 'Hiệu lực', 'Lượt dùng', 'Trạng thái', ''].map(label =>
                  <th key={label} className="text-left px-4 py-3 font-medium text-gray-600">{label}</th>)}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {vouchers.map(voucher => {
                const expired = new Date(voucher.ngayKetThuc) < new Date();
                return (
                  <tr key={voucher.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3"><p className="font-semibold text-gray-900">{voucher.maVoucher}</p><p className="text-xs text-gray-400">{voucher.moTa}</p></td>
                    <td className="px-4 py-3 font-medium">{voucher.loaiGiam === 'PERCENT' ? `${voucher.giaTriGiam}%` : formatCurrency(voucher.giaTriGiam)}</td>
                    <td className="px-4 py-3 text-gray-500">Từ {formatCurrency(voucher.giaTriToiThieu || 0)}<br />Tối đa {formatCurrency(voucher.giaTriGiamToiDa || 0)}</td>
                    <td className="px-4 py-3 text-gray-500">{formatDate(voucher.ngayBatDau)}<br />{formatDate(voucher.ngayKetThuc)}</td>
                    <td className="px-4 py-3">{voucher.soLuongSuDung || 0}/{voucher.soLuongToiDa ?? '∞'}</td>
                    <td className="px-4 py-3"><span className={`badge badge-${voucher.trangThai && !expired ? 'green' : 'gray'} text-xs`}>{expired ? 'Hết hạn' : voucher.trangThai ? 'Hoạt động' : 'Đã tắt'}</span></td>
                    <td className="px-4 py-3"><div className="flex gap-2">
                      <button onClick={() => openEdit(voucher)} className="p-1.5 text-gray-400 hover:text-blue-600" aria-label={`Sửa ${voucher.maVoucher}`}><Edit2 className="w-4 h-4" /></button>
                      <button onClick={() => remove(voucher)} className="p-1.5 text-gray-400 hover:text-red-600" aria-label={`Ngừng ${voucher.maVoucher}`}><Trash2 className="w-4 h-4" /></button>
                    </div></td>
                  </tr>
                );
              })}
              {vouchers.length === 0 && <tr><td colSpan={7} className="text-center py-12 text-gray-400">Chưa có voucher</td></tr>}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="voucher-form-title">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 id="voucher-form-title" className="font-bold text-gray-900">{editing ? 'Cập nhật voucher' : 'Tạo voucher'}</h3>
              <button onClick={() => setShowForm(false)} className="p-1 text-gray-400 hover:text-gray-700" aria-label="Đóng"><X className="w-5 h-5" /></button>
            </div>
            <form onSubmit={save} className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Mã voucher *</label><input className="input-field uppercase" required maxLength={50} disabled={!!editing} value={form.maVoucher} onChange={e => setForm({ ...form, maVoucher: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Loại giảm *</label><select className="input-field" value={form.loaiGiam} onChange={e => setForm({ ...form, loaiGiam: e.target.value as VoucherForm['loaiGiam'] })}><option value="PERCENT">Phần trăm</option><option value="FIXED">Số tiền</option></select></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Giá trị giảm *</label><input type="number" className="input-field" required min="1" max={form.loaiGiam === 'PERCENT' ? 100 : undefined} value={form.giaTriGiam} onChange={e => setForm({ ...form, giaTriGiam: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Giảm tối đa *</label><input type="number" className="input-field" required min="0" value={form.giaTriGiamToiDa} onChange={e => setForm({ ...form, giaTriGiamToiDa: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Đơn tối thiểu</label><input type="number" className="input-field" min="0" value={form.giaTriToiThieu} onChange={e => setForm({ ...form, giaTriToiThieu: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Số lượt tối đa</label><input type="number" className="input-field" min="1" placeholder="Không giới hạn" value={form.soLuongToiDa} onChange={e => setForm({ ...form, soLuongToiDa: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Bắt đầu *</label><input type="datetime-local" className="input-field" required value={form.ngayBatDau} onChange={e => setForm({ ...form, ngayBatDau: e.target.value })} /></div>
              <div><label className="block text-sm font-medium text-gray-700 mb-1">Kết thúc *</label><input type="datetime-local" className="input-field" required value={form.ngayKetThuc} onChange={e => setForm({ ...form, ngayKetThuc: e.target.value })} /></div>
              <div className="sm:col-span-2"><label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label><textarea className="input-field resize-none" rows={3} value={form.moTa} onChange={e => setForm({ ...form, moTa: e.target.value })} /></div>
              <label className="sm:col-span-2 flex items-center gap-2 text-sm text-gray-700"><input type="checkbox" checked={form.trangThai} onChange={e => setForm({ ...form, trangThai: e.target.checked })} /> Hoạt động</label>
              <div className="sm:col-span-2 flex justify-end gap-3"><button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Hủy</button><button type="submit" disabled={saving} className="btn-primary">{saving ? 'Đang lưu...' : 'Lưu voucher'}</button></div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
