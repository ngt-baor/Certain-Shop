import { useCallback, useEffect, useState } from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { adminApi } from '../../services/api';
import type { TonKhoCanhBao } from '../../services/api';
import { formatCurrency } from '../../utils/format';
import LoadingSpinner from '../../components/LoadingSpinner';
import toast from 'react-hot-toast';

interface ThongKe {
  doanhThuThang: number;
  doanhThuHomNay: number;
  thongKeTrangThai: Record<string, number>;
  tongKhachHang: number;
}

type DoanhThuNgay = [string, number];
type SanPhamBanChay = [number, string, number];

const dateInput = (date: Date) => {
  const offset = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 10);
};

export default function DashboardPage() {
  const now = new Date();
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
  const [data, setData] = useState<ThongKe | null>(null);
  const [tuNgay, setTuNgay] = useState(dateInput(firstDay));
  const [denNgay, setDenNgay] = useState(dateInput(now));
  const [doanhThu, setDoanhThu] = useState<{ chiTiet: DoanhThuNgay[]; tongDoanhThu: number }>({ chiTiet: [], tongDoanhThu: 0 });
  const [banChay, setBanChay] = useState<SanPhamBanChay[]>([]);
  const [sapHet, setSapHet] = useState<TonKhoCanhBao[]>([]);
  const [hetHang, setHetHang] = useState<TonKhoCanhBao[]>([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    if (tuNgay > denNgay) {
      toast.error('Ngày bắt đầu không được sau ngày kết thúc');
      return;
    }
    setLoading(true);
    try {
      const [tongQuan, revenue, best, low, out] = await Promise.all([
        adminApi.tongQuan(),
        adminApi.doanhThu(`${tuNgay}T00:00:00`, `${denNgay}T23:59:59`),
        adminApi.sanPhamBanChay(),
        adminApi.sanPhamSapHetHang(),
        adminApi.sanPhamHetHang(),
      ]);
      setData((tongQuan.data.duLieu || null) as unknown as ThongKe | null);
      setDoanhThu((revenue.data.duLieu || { chiTiet: [], tongDoanhThu: 0 }) as { chiTiet: DoanhThuNgay[]; tongDoanhThu: number });
      setBanChay((best.data.duLieu || []) as SanPhamBanChay[]);
      setSapHet(low.data.duLieu || []);
      setHetHang(out.data.duLieu || []);
    } catch {
      toast.error('Không thể tải đầy đủ dữ liệu thống kê');
    } finally {
      setLoading(false);
    }
  }, [tuNgay, denNgay]);

  useEffect(() => { load(); }, [load]);

  if (loading && !data) return <LoadingSpinner fullPage />;

  const choXacNhan = data?.thongKeTrangThai?.CHO_XAC_NHAN ?? 0;
  const tongDonHang = Object.values(data?.thongKeTrangThai || {}).reduce((a, b) => a + b, 0);
  const cards = [
    { label: 'Doanh thu hôm nay', value: formatCurrency(data?.doanhThuHomNay ?? 0) },
    { label: 'Doanh thu tháng', value: formatCurrency(data?.doanhThuThang ?? 0) },
    { label: 'Tổng đơn hàng', value: tongDonHang },
    { label: 'Khách hàng', value: data?.tongKhachHang ?? 0 },
  ];

  return (
    <div>
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4 mb-6">
        <div><h2 className="text-xl font-bold text-gray-900">Tổng quan</h2><p className="text-sm text-gray-500 mt-1">Doanh thu, sản phẩm bán chạy và cảnh báo tồn kho.</p></div>
        <div className="flex flex-wrap items-end gap-2">
          <label className="text-xs text-gray-500">Từ ngày<input type="date" className="input-field text-sm mt-1" value={tuNgay} onChange={e => setTuNgay(e.target.value)} /></label>
          <label className="text-xs text-gray-500">Đến ngày<input type="date" className="input-field text-sm mt-1" value={denNgay} onChange={e => setDenNgay(e.target.value)} /></label>
          <button onClick={load} disabled={loading} className="btn-secondary text-sm flex items-center gap-2"><RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Làm mới</button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-6">
        {cards.map(card => <div key={card.label} className="bg-white rounded-xl border border-gray-100 p-5"><span className="text-sm font-medium text-gray-500">{card.label}</span><p className="text-2xl font-bold text-gray-900 mt-4">{card.value}</p></div>)}
      </div>

      {choXacNhan > 0 && <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 flex items-center gap-3 mb-6"><AlertCircle className="w-5 h-5 text-yellow-600" /><p className="text-yellow-800 text-sm">Có <strong>{choXacNhan}</strong> đơn hàng đang chờ xác nhận.</p></div>}

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
        <section className="bg-white rounded-xl border border-gray-100 p-5">
          <div className="flex justify-between items-center mb-4"><h3 className="font-semibold text-gray-900">Doanh thu theo ngày</h3><strong className="text-indigo-600">{formatCurrency(doanhThu.tongDoanhThu || 0)}</strong></div>
          <div className="max-h-72 overflow-y-auto"><table className="w-full text-sm"><thead className="text-gray-500 border-b"><tr><th className="text-left py-2">Ngày</th><th className="text-right py-2">Doanh thu</th></tr></thead><tbody>{doanhThu.chiTiet.map((row, index) => <tr key={`${row[0]}-${index}`} className="border-b border-gray-50"><td className="py-2">{String(row[0])}</td><td className="text-right font-medium">{formatCurrency(Number(row[1]))}</td></tr>)}{doanhThu.chiTiet.length === 0 && <tr><td colSpan={2} className="text-center py-8 text-gray-400">Chưa có doanh thu hoàn tất</td></tr>}</tbody></table></div>
        </section>

        <section className="bg-white rounded-xl border border-gray-100 p-5">
          <h3 className="font-semibold text-gray-900 mb-4">Sản phẩm bán chạy 30 ngày</h3>
          <div className="space-y-3">{banChay.slice(0, 10).map((row, index) => <div key={row[0]} className="flex items-center gap-3"><span className="w-7 h-7 rounded-full bg-indigo-50 text-indigo-600 text-xs font-bold flex items-center justify-center">{index + 1}</span><span className="flex-1 text-sm text-gray-700">{row[1]}</span><strong className="text-sm">{row[2]} sp</strong></div>)}{banChay.length === 0 && <p className="text-center py-8 text-gray-400 text-sm">Chưa có dữ liệu</p>}</div>
        </section>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <StockTable title="Sắp hết hàng" items={sapHet} color="yellow" />
        <StockTable title="Đã hết hàng" items={hetHang} color="red" />
      </div>
    </div>
  );
}

function StockTable({ title, items, color }: { title: string; items: TonKhoCanhBao[]; color: 'yellow' | 'red' }) {
  return (
    <section className="bg-white rounded-xl border border-gray-100 p-5">
      <div className="flex items-center justify-between mb-4"><h3 className="font-semibold text-gray-900">{title}</h3><span className={`badge badge-${color} text-xs`}>{items.length}</span></div>
      <div className="max-h-72 overflow-y-auto space-y-2">{items.slice(0, 20).map(item => <div key={item.bienTheId} className="flex items-center justify-between gap-3 py-2 border-b border-gray-50"><div><p className="text-sm font-medium text-gray-800">{item.tenSanPham}</p><p className="text-xs text-gray-400">{[item.mauSac, item.kichThuoc].filter(Boolean).join(' · ') || `Biến thể #${item.bienTheId}`}</p></div><strong className={item.soLuongTon === 0 ? 'text-red-600' : 'text-yellow-600'}>{item.soLuongTon}</strong></div>)}{items.length === 0 && <p className="text-center py-8 text-gray-400 text-sm">Không có sản phẩm</p>}</div>
    </section>
  );
}
