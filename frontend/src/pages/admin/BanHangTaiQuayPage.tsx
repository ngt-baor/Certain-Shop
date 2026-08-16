import { useState, useEffect, useCallback } from 'react';
import {
    Search, Plus, Trash2, CreditCard, Banknote, User,
    ShoppingCart, Package, X,
    Hash, Minus, PlusCircle
} from 'lucide-react';
import { banHangApi } from '../../services/api';
import { formatCurrency, getImageUrl, handleImgError } from '../../utils/format';
import toast from 'react-hot-toast';
import LoadingSpinner from '../../components/LoadingSpinner';

interface HoaDonCho {
    id: number;
    maDonHang: string;
    tongTienThanhToan: number;
}

interface ChiTietHoaDon {
    id: number;
    bienTheId: number;
    tenSanPham: string;
    kichThuoc: string;
    mauSac: string;
    soLuong: number;
    donGia: number;
    thanhTien: number;
    anhUrl: string;
}

interface SanPhamTaiQuay {
    id: number;
    tenSanPham: string;
    mauSac: string;
    kichThuoc?: string;
    soLuongTon: number;
    giaBan: number;
    anhUrl: string;
}

const layThongBaoLoi = (error: unknown, fallback: string) =>
    (error as { response?: { data?: { thongBao?: string } } })
        .response?.data?.thongBao || fallback;

export default function BanHangTaiQuayPage() {
    const [danhSachCho, setDanhSachCho] = useState<HoaDonCho[]>([]);
    const [selectedHd, setSelectedHd] = useState<number | null>(null);
    const [chiTiet, setChiTiet] = useState<ChiTietHoaDon[]>([]);
    const [loading, setLoading] = useState(true);
    const [loadingAction, setLoadingAction] = useState(false);

    // Search state
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState<SanPhamTaiQuay[]>([]);

    // Payment state
    const [phuongThuc, setPhuongThuc] = useState('TIEN_MAT');
    const [tienKhachDua, setTienKhachDua] = useState<number>(0);
    const [tenKhach, setTenKhach] = useState('');
    const [sdtKhach, setSdtKhach] = useState('');
    const [maVoucher, setMaVoucher] = useState('');
    const [soTienGiam, setSoTienGiam] = useState(0);

    const refreshDanhSachCho = useCallback(async () => {
        try {
            const res = await banHangApi.danhSachCho();
            const list = res.data as unknown as HoaDonCho[];
            setDanhSachCho(list);
            if (list.length > 0 && !selectedHd) {
                setSelectedHd(list[0].id);
            } else if (list.length === 0) {
                setSelectedHd(null);
                setChiTiet([]);
            }
        } catch {
            toast.error('Không thể tải danh sách hóa đơn chờ');
        } finally {
            setLoading(false);
        }
    }, [selectedHd]);

    const refreshChiTiet = useCallback(async (id: number) => {
        try {
            const res = await banHangApi.chiTietHoaDon(id);
            if (res.data.thanhCong) {
                setChiTiet(res.data.chiTiet as ChiTietHoaDon[]);
                // Reset payment info when switching invoice
                setSoTienGiam(0);
                setMaVoucher('');
            }
        } catch {
            toast.error('Không thể tải chi tiết hóa đơn');
        }
    }, []);

    useEffect(() => {
        refreshDanhSachCho();
    }, [refreshDanhSachCho]);

    useEffect(() => {
        if (selectedHd) {
            refreshChiTiet(selectedHd);
        }
    }, [selectedHd, refreshChiTiet]);

    const handleTaoHoaDon = async () => {
        if (danhSachCho.length >= 5) {
            toast.error('Chỉ được tạo tối đa 5 hóa đơn chờ');
            return;
        }
        setLoadingAction(true);
        try {
            const res = await banHangApi.taoHoaDon();
            if (res.data.thanhCong) {
                toast.success('Đã tạo hóa đơn mới');
                setSelectedHd(res.data.idHoaDon);
                refreshDanhSachCho();
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi khi tạo hóa đơn'));
        } finally {
            setLoadingAction(false);
        }
    };

    const handleSearch = async (val: string) => {
        setSearchTerm(val);
        if (val.length < 2) {
            setSearchResults([]);
            return;
        }
        try {
            const res = await banHangApi.timSanPham(val);
            setSearchResults(res.data as SanPhamTaiQuay[]);
        } catch (error) {
            console.error(error);
        }
    };

    const addSanPham = async (bienTheId: number) => {
        if (!selectedHd) {
            toast.error('Vui lòng chọn hoặc tạo hóa đơn');
            return;
        }
        try {
            const res = await banHangApi.themSanPham(selectedHd, bienTheId, 1);
            if (res.data.thanhCong) {
                toast.success('Đã thêm vào giỏ');
                refreshChiTiet(selectedHd);
                setSearchTerm('');
                setSearchResults([]);
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi khi thêm sản phẩm'));
        }
    };

    const updateSoLuong = async (chiTietId: number, moi: number) => {
        if (moi < 1) return;
        try {
            const res = await banHangApi.capNhatSoLuong(chiTietId, moi);
            if (res.data.thanhCong && selectedHd) {
                refreshChiTiet(selectedHd);
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi cập nhật'));
        }
    };

    const xoaSanPham = async (chiTietId: number) => {
        try {
            const res = await banHangApi.xoaSanPham(chiTietId);
            if (res.data.thanhCong && selectedHd) {
                toast.success('Đã xóa sản phẩm');
                refreshChiTiet(selectedHd);
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi khi xóa'));
        }
    };

    const apVoucher = async () => {
        if (!selectedHd || !maVoucher) return;
        try {
            const res = await banHangApi.apVoucher(selectedHd, maVoucher);
            if (res.data.thanhCong) {
                setSoTienGiam(res.data.soTienGiam);
                toast.success(res.data.thongBao);
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Voucher không hợp lệ'));
        }
    };

    const handleThanhToan = async () => {
        if (!selectedHd) return;
        if (chiTiet.length === 0) {
            toast.error('Giỏ hàng trống');
            return;
        }

        const tongTien = chiTiet.reduce((a, b) => a + b.thanhTien, 0) - soTienGiam;
        if (phuongThuc === 'TIEN_MAT' && tienKhachDua < tongTien) {
            toast.error('Tiền khách đưa không đủ');
            return;
        }

        setLoadingAction(true);
        try {
            const res = await banHangApi.thanhToan(selectedHd, {
                phuongThucThanhToan: phuongThuc,
                tienKhachDua,
                tenKhach,
                sdtKhach
            });
            if (res.data.thanhCong) {
                toast.success('Thanh toán thành công');
                // Reset local states
                setTenKhach('');
                setSdtKhach('');
                setTienKhachDua(0);
                setSelectedHd(null);
                refreshDanhSachCho();
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi thanh toán'));
        } finally {
            setLoadingAction(false);
        }
    };

    const handleHuyHoaDon = async () => {
        if (!selectedHd) return;
        if (!window.confirm('Bạn có chắc muốn hủy hóa đơn này?')) return;

        try {
            const res = await banHangApi.huyHoaDon(selectedHd);
            if (res.data.thanhCong) {
                toast.success('Đã hủy hóa đơn');
                setSelectedHd(null);
                refreshDanhSachCho();
            }
        } catch (error: unknown) {
            toast.error(layThongBaoLoi(error, 'Lỗi khi hủy'));
        }
    };

    const tongTienHang = chiTiet.reduce((a, b) => a + b.thanhTien, 0);
    const tongThanhToan = tongTienHang - soTienGiam;

    if (loading) return <LoadingSpinner fullPage />;

    return (
        <div className="flex flex-col h-[calc(100vh-120px)] gap-4">
            {/* Header Tabs */}
            <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
                {danhSachCho.map((hd) => (
                    <button
                        key={hd.id}
                        onClick={() => setSelectedHd(hd.id)}
                        className={`flex items-center gap-2 px-4 py-2 rounded-lg border whitespace-nowrap transition-all ${
                            selectedHd === hd.id
                                ? 'bg-indigo-600 border-indigo-600 text-white shadow-md'
                                : 'bg-white border-gray-200 text-gray-600 hover:border-indigo-300'
                        }`}
                    >
                        <Hash className="w-4 h-4" />
                        <span className="font-medium">{hd.maDonHang}</span>
                        {selectedHd === hd.id && (
                            <X
                                className="w-4 h-4 hover:text-red-200"
                                onClick={(e) => { e.stopPropagation(); handleHuyHoaDon(); }}
                            />
                        )}
                    </button>
                ))}
                <button
                    onClick={handleTaoHoaDon}
                    disabled={loadingAction}
                    className="p-2 bg-indigo-50 text-indigo-600 rounded-lg border border-indigo-100 hover:bg-indigo-100 transition-colors"
                    title="Tạo hóa đơn mới"
                >
                    <Plus className="w-5 h-5" />
                </button>
            </div>

            {!selectedHd ? (
                <div className="flex-1 bg-white rounded-2xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center text-gray-400">
                    <ShoppingCart className="w-16 h-16 mb-4 opacity-20" />
                    <p className="text-lg font-medium">Chưa có hóa đơn nào được chọn</p>
                    <button onClick={handleTaoHoaDon} className="mt-4 btn-primary">Tạo hóa đơn mới ngay</button>
                </div>
            ) : (
                <div className="flex flex-1 gap-4 overflow-hidden">
                    {/* Main Content - Left */}
                    <div className="flex-[2.5] flex flex-col gap-4 overflow-hidden">
                        {/* Search Box */}
                        <div className="bg-white p-4 rounded-xl border border-gray-100 shadow-sm relative z-20">
                            <div className="relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                                <input
                                    type="text"
                                    placeholder="Tìm kiếm sản phẩm theo tên, mã sản phẩm... (F2)"
                                    className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border-none rounded-lg focus:ring-2 focus:ring-indigo-500 text-sm"
                                    value={searchTerm}
                                    onChange={(e) => handleSearch(e.target.value)}
                                />
                            </div>

                            {/* Search Results Dropdown */}
                            {searchResults.length > 0 && (
                                <div className="absolute top-full left-0 right-0 mt-2 bg-white rounded-xl shadow-2xl border border-gray-100 max-h-96 overflow-y-auto z-50">
                                    {searchResults.map((item) => (
                                        <div
                                            key={item.id}
                                            onClick={() => addSanPham(item.id)}
                                            className="flex items-center gap-4 p-3 hover:bg-gray-50 cursor-pointer border-b last:border-0 border-gray-50"
                                        >
                                            <img
                                                src={getImageUrl(item.anhUrl)}
                                                onError={handleImgError}
                                                className="w-12 h-12 rounded object-cover border"
                                                alt=""
                                            />
                                            <div className="flex-1">
                                                <p className="font-bold text-gray-900 text-sm">{item.tenSanPham}</p>
                                                <p className="text-xs text-gray-500">
                                                    {item.mauSac} {item.kichThuoc ? `/ ${item.kichThuoc}` : ''} • Tồn: {item.soLuongTon}
                                                </p>
                                            </div>
                                            <p className="font-bold text-indigo-600">{formatCurrency(item.giaBan)}</p>
                                            <PlusCircle className="w-5 h-5 text-indigo-400" />
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Cart Table */}
                        <div className="flex-1 bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden flex flex-col">
                            <div className="p-4 border-b border-gray-50 flex justify-between items-center bg-gray-50/50">
                                <h3 className="font-bold text-gray-900 flex items-center gap-2">
                                    <Package className="w-5 h-5 text-indigo-500" />
                                    Danh sách sản phẩm ({chiTiet.length})
                                </h3>
                            </div>
                            <div className="flex-1 overflow-y-auto">
                                {chiTiet.length === 0 ? (
                                    <div className="h-full flex flex-col items-center justify-center text-gray-400">
                                        <p>Chưa có sản phẩm nào trong hóa đơn</p>
                                    </div>
                                ) : (
                                    <table className="w-full text-sm">
                                        <thead className="sticky top-0 bg-white border-b border-gray-100 shadow-sm z-10">
                                        <tr>
                                            <th className="text-left px-4 py-3 font-medium text-gray-500 w-12 text-center">#</th>
                                            <th className="text-left px-4 py-3 font-medium text-gray-500">Sản phẩm</th>
                                            <th className="text-center px-4 py-3 font-medium text-gray-500 w-32">Đơn giá</th>
                                            <th className="text-center px-4 py-3 font-medium text-gray-500 w-36">Số lượng</th>
                                            <th className="text-right px-4 py-3 font-medium text-gray-500 w-32">Thành tiền</th>
                                            <th className="w-12"></th>
                                        </tr>
                                        </thead>
                                        <tbody className="divide-y divide-gray-50">
                                        {chiTiet.map((item, idx) => (
                                            <tr key={item.id} className="hover:bg-gray-50/50 transition-colors">
                                                <td className="px-4 py-4 text-center text-gray-400">{idx + 1}</td>
                                                <td className="px-4 py-4">
                                                    <div className="flex gap-3">
                                                        <img
                                                            src={getImageUrl(item.anhUrl)}
                                                            onError={handleImgError}
                                                            className="w-12 h-12 rounded object-cover border bg-gray-100"
                                                            alt=""
                                                        />
                                                        <div>
                                                            <p className="font-bold text-gray-900 leading-tight">{item.tenSanPham}</p>
                                                            <p className="text-xs text-gray-500 mt-1">
                                                                {item.mauSac} {item.kichThuoc ? `| ${item.kichThuoc}` : ''}
                                                            </p>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-4 text-center font-medium text-gray-700">
                                                    {formatCurrency(item.donGia)}
                                                </td>
                                                <td className="px-4 py-4">
                                                    <div className="flex items-center justify-center gap-2">
                                                        <button
                                                            onClick={() => updateSoLuong(item.id, item.soLuong - 1)}
                                                            className="p-1 hover:text-indigo-600 bg-gray-100 rounded"
                                                        >
                                                            <Minus className="w-4 h-4" />
                                                        </button>
                                                        <span className="w-8 text-center font-bold">{item.soLuong}</span>
                                                        <button
                                                            onClick={() => updateSoLuong(item.id, item.soLuong + 1)}
                                                            className="p-1 hover:text-indigo-600 bg-gray-100 rounded"
                                                        >
                                                            <Plus className="w-4 h-4" />
                                                        </button>
                                                    </div>
                                                </td>
                                                <td className="px-4 py-4 text-right font-bold text-indigo-600">
                                                    {formatCurrency(item.thanhTien)}
                                                </td>
                                                <td className="px-4 py-4 text-center">
                                                    <button
                                                        onClick={() => xoaSanPham(item.id)}
                                                        className="text-gray-300 hover:text-red-500 transition-colors"
                                                    >
                                                        <Trash2 className="w-4 h-4" />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Sidebar - Right */}
                    <div className="flex-1 flex flex-col gap-4 overflow-y-auto pr-1">
                        {/* Customer Info */}
                        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
                            <div className="p-4 bg-gray-50/50 border-b border-gray-50 flex items-center gap-2">
                                <User className="w-4 h-4 text-gray-400" />
                                <h3 className="text-sm font-bold text-gray-900">Thông tin khách hàng</h3>
                            </div>
                            <div className="p-4 space-y-3">
                                <input
                                    type="text"
                                    placeholder="Tên khách hàng"
                                    className="w-full text-sm border-gray-200 rounded-lg focus:ring-indigo-500"
                                    value={tenKhach}
                                    onChange={e => setTenKhach(e.target.value)}
                                />
                                <input
                                    type="text"
                                    placeholder="Số điện thoại"
                                    className="w-full text-sm border-gray-200 rounded-lg focus:ring-indigo-500"
                                    value={sdtKhach}
                                    onChange={e => setSdtKhach(e.target.value)}
                                />
                            </div>
                        </div>

                        {/* Voucher */}
                        <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
                            <div className="p-4 bg-gray-50/50 border-b border-gray-50 flex items-center gap-2">
                                <CreditCard className="w-4 h-4 text-gray-400" />
                                <h3 className="text-sm font-bold text-gray-900">Mã giảm giá</h3>
                            </div>
                            <div className="p-4 flex gap-2">
                                <input
                                    type="text"
                                    placeholder="Nhập mã voucher"
                                    className="flex-1 text-sm border-gray-200 rounded-lg focus:ring-indigo-500 uppercase"
                                    value={maVoucher}
                                    onChange={e => setMaVoucher(e.target.value.toUpperCase())}
                                />
                                <button
                                    onClick={apVoucher}
                                    className="px-3 py-2 bg-indigo-50 text-indigo-600 rounded-lg font-bold text-xs hover:bg-indigo-100"
                                >
                                    Áp dụng
                                </button>
                            </div>
                            {soTienGiam > 0 && (
                                <div className="px-4 pb-4 flex justify-between items-center text-xs">
                                    <span className="text-green-600 font-medium">✓ Đã giảm: {formatCurrency(soTienGiam)}</span>
                                    <button onClick={() => setSoTienGiam(0)} className="text-red-400 hover:underline">Hủy</button>
                                </div>
                            )}
                        </div>

                        {/* Payment Summary */}
                        <div className="bg-white rounded-xl border border-indigo-100 shadow-xl overflow-hidden mt-auto">
                            <div className="p-6 space-y-4">
                                <div className="flex justify-between text-sm text-gray-500">
                                    <span>Tổng tiền hàng:</span>
                                    <span className="font-bold text-gray-900">{formatCurrency(tongTienHang)}</span>
                                </div>
                                {soTienGiam > 0 && (
                                    <div className="flex justify-between text-sm text-green-600">
                                        <span>Giảm giá:</span>
                                        <span className="font-bold">-{formatCurrency(soTienGiam)}</span>
                                    </div>
                                )}
                                <div className="pt-4 border-t border-dashed border-gray-100 flex justify-between items-center">
                                    <span className="text-gray-900 font-bold">Khách cần trả:</span>
                                    <span className="text-2xl font-black text-indigo-600">{formatCurrency(tongThanhToan)}</span>
                                </div>

                                <div className="grid grid-cols-2 gap-2 pt-2">
                                    <button
                                        onClick={() => setPhuongThuc('TIEN_MAT')}
                                        className={`flex flex-col items-center gap-1 p-3 rounded-xl border-2 transition-all ${
                                            phuongThuc === 'TIEN_MAT'
                                                ? 'border-green-500 bg-green-50 text-green-700'
                                                : 'border-gray-100 hover:border-gray-200 text-gray-400'
                                        }`}
                                    >
                                        <Banknote className="w-6 h-6" />
                                        <span className="text-xs font-bold">Tiền mặt</span>
                                    </button>
                                    <button
                                        onClick={() => setPhuongThuc('CHUYEN_KHOAN')}
                                        className={`flex flex-col items-center gap-1 p-3 rounded-xl border-2 transition-all ${
                                            phuongThuc === 'CHUYEN_KHOAN'
                                                ? 'border-blue-500 bg-blue-50 text-blue-700'
                                                : 'border-gray-100 hover:border-gray-200 text-gray-400'
                                        }`}
                                    >
                                        <CreditCard className="w-6 h-6" />
                                        <span className="text-xs font-bold">Chuyển khoản</span>
                                    </button>
                                </div>

                                {phuongThuc === 'TIEN_MAT' && (
                                    <div className="space-y-3 pt-2">
                                        <div>
                                            <label className="text-xs text-gray-400 block mb-1">Tiền khách đưa:</label>
                                            <input
                                                type="number"
                                                className="w-full text-xl font-bold border-gray-200 rounded-lg focus:ring-green-500 text-right"
                                                value={tienKhachDua}
                                                onChange={e => setTienKhachDua(Number(e.target.value))}
                                            />
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Tiền thừa trả khách:</span>
                                            <span className={`font-bold ${tienKhachDua >= tongThanhToan ? 'text-green-600' : 'text-red-400'}`}>
                        {formatCurrency(Math.max(0, tienKhachDua - tongThanhToan))}
                      </span>
                                        </div>
                                    </div>
                                )}

                                <button
                                    onClick={handleThanhToan}
                                    disabled={loadingAction || chiTiet.length === 0}
                                    className="w-full py-4 bg-indigo-600 text-white rounded-xl font-black text-lg shadow-lg shadow-indigo-200 hover:bg-indigo-700 active:scale-[0.98] transition-all disabled:opacity-50 disabled:grayscale"
                                >
                                    {loadingAction ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN THANH TOÁN (F12)'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
