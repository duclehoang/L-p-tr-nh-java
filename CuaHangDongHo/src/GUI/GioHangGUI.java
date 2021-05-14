package GUI;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import Button.*;
import DAO.*;
import BUS.*;
import DTO.*;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;
public class GioHangGUI extends JPanel{
	private JTextField tfmahd = new JTextField(20);
	private String MaHD = "^HD[1-9]{1,}";
	//private JTextField tfmanv = new JTextField(20);
	//private JTextField tfmakh = new JTextField(20);
	private JComboBox cbmakh = new JComboBox(); 
	private JComboBox cbmanv = new JComboBox();
	private JTextField tfngaylap = new JTextField(20);
	private JTextField tongtien = new JTextField(30); 
	JButton sua = new ButtonChange();
	JButton xoa = new ButtonRemove();
	JButton thanhtoan = new ButtonThanhToan();
	
	private JTable giohangTable = new JTable();
	private DefaultTableModel model = new DefaultTableModel();
	private JScrollPane scr = new JScrollPane();
	HoaDonBUS hdbus = new HoaDonBUS();
	cthdBUS ctbus ;
	SanPhamBUS spBUS = new SanPhamBUS();
	
	NhanVienBUS nvBUS = new NhanVienBUS();
	KhachHangBLL khBUS =new KhachHangBLL();
	
	public GioHangGUI(ArrayList<SanPham> list1) {
		ArrayList<SanPham> list = list1;
		JPanel ThongTin = CreateTop(list);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		//table
		giohangTable.setModel(model);
		model.addColumn("Mã sản phẩm");
		model.addColumn("Tên sản phẩm");
		model.addColumn("Số lượng");
		model.addColumn("Đơn giá (vnd)");
		model.addColumn("Thành tiền (vnd)");
		giohangTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		giohangTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		giohangTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		giohangTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		giohangTable.getColumnModel().getColumn(4).setPreferredWidth(150);
		ShowSP(list);
		scr = new JScrollPane(giohangTable);
		
		
		JPanel bot = new JPanel();
		JPanel botbot = new JPanel();
		
		
		thanhtoan.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				Thanhtoan(me,list);
				
			}
		});
		botbot.add(thanhtoan);
		JLabel label = new JLabel("Tổng tiền (vnd): ");
		tongtien.setEditable(false);
		//tinh tong tien
		float s=0;
		for(SanPham sp : list) {
			float tien = sp.getSoluong()*sp.getDongia();
			s+=tien;
		}
		tongtien.setText(String.valueOf(s));
		bot.add(label);
		bot.add(tongtien);
		add(ThongTin);
		add(scr);
		add(bot);
		add(botbot);
		
	}
	private JPanel CreateTop(ArrayList<SanPham> list) {
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		tfmahd.setBorder(BorderFactory.createTitledBorder(border,"Mã hóa đơn"));
		//tfmanv.setBorder(BorderFactory.createTitledBorder(border,"Mã nhân viên"));
		//tfmakh.setBorder(BorderFactory.createTitledBorder(border,"Mã khách hàng"));
				
		cbmanv.setBorder(BorderFactory.createTitledBorder(border,"Mã nhân viên"));
		nvBUS.docDSNV();		
		for(NhanVienDTO nv : nvBUS.dsnv) {
			cbmanv.addItem(nv.MaNV);
		}
		
		cbmakh.setBorder(BorderFactory.createTitledBorder(border,"Mã khách hàng"));
		for(KhachHangDTO kh: khBUS.getAllKhachang()) {
			//chỉnh danh sách khách hàng
			cbmakh.addItem(kh.getMaKH());
		}
		tfngaylap.setBorder(BorderFactory.createTitledBorder(border,"Ngày lập"));
		tfngaylap.setEditable(false);
		tfngaylap.setText(String.valueOf(java.time.LocalDate.now()));
		
		JPanel on = new JPanel();
		JPanel below = new JPanel();
		JPanel bot = new JPanel();
		JLabel lb= new JLabel("");	//label dư để căn lề
		JTextField tx = new JTextField();
		tx.setEditable(false);
		
		on.setLayout(new GridLayout(0,4,1,1));
		on.add(tx);
		on.add(tfmahd);
		on.add(cbmanv);
		
		
		below.setLayout(new GridLayout(0,4));
		below.add(lb);
		below.add(cbmakh);
		//below.add(tfmakh);
		below.add(tfngaylap);
				
		
		xoa.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				Xoa(me, list);
			}
		});
		
		bot.add(xoa);
		
		
		top.add(on);
		top.add(below);
		top.add(bot);
		return top;
	}
	private void Thanhtoan(MouseEvent me,ArrayList<SanPham> list) {
		if(tfmahd.getText().equals("") || String.valueOf(cbmakh.getSelectedItem()).equals("") || String.valueOf(cbmanv.getSelectedItem()).equals("")) {
			JOptionPane.showMessageDialog(null, "Chưa điền đầy đủ thông tin");
			return;
		}
		else {
			//thêm hóa đơn
			String mahd = tfmahd.getText();
			if(!MaHD(mahd)) {
				JOptionPane.showMessageDialog(null, "Mã hóa đơn phải là <HD*>");
				return;
			}
			String manv = (String)cbmanv.getSelectedItem();
			String makh = (String)cbmakh.getSelectedItem();
			String ngaylap = tfngaylap.getText();
			float tien = Float.parseFloat(tongtien.getText());
			HoaDon hoadon = new HoaDon(mahd, manv, makh, ngaylap, tien);
			if(hdbus.Add(hoadon)) {
				//thêm chi tiết hóa đơn
				for(SanPham sanpham: list) {	// danh sách sản phẩm trong giỏ hàng
					ctbus = new cthdBUS(hoadon.getMaHD());	//chi tiết bus
					//tạo chi tiết hd mới từ sản phẩm trong list
					cthd ct = new cthd(mahd, sanpham.masp, sanpham.dongia, sanpham.soluong, sanpham.dongia*sanpham.soluong);
					//thêm chi tiết vào database
					if(ctbus.Add(ct, mahd)) {
						//sửa số lượng của sản phẩm đã bán
						for(SanPham sanp : spBUS.getDssp()) {	//danh sách sản phẩm chính
							if(sanp.getMasp().equals(sanpham.getMasp())) {	// nếu sản phẩm là sản phẩm đang xét
								//update
								if(spBUS.update(sanp.getMasp(), sanp.getTensp(), sanp.getMaloai(), sanp.getDongia(), sanp.getSoluong()-sanpham.soluong, sanp.getHinhanh())) {
									System.out.println();
								}
							}
						}
						
					}
					else {
						System.out.println("Thêm chi tiết không thành công"+ct.getMaSP());
						return;
					}
				}
			}
			
			else {
				JOptionPane.showMessageDialog(null, "Thêm hóa đơn không thành công");
				return;
			}
			JOptionPane.showMessageDialog(null, "Thanh toán thành công");
		}
	}
	
	private void Xoa(MouseEvent me,ArrayList<SanPham> list) {
		int i = giohangTable.getSelectedRow();
		if(i>=0) {
			boolean isRemove =list.removeIf(t->t.getMasp().equals(model.getValueAt(i, 0)));
			if(isRemove) {
				model.removeRow(i);
				JOptionPane.showMessageDialog(null, "Xóa thànhh công");
				for(SanPham sanpham: list)
				{
					System.out.println(sanpham.masp);
				}
			}
		}
		
	}

	private void ShowSP(ArrayList<SanPham> list) {
		for(SanPham sanpham: list) {
			Object[] obj = {sanpham.getMasp(), sanpham.getTensp(), sanpham.getSoluong(), sanpham.getDongia(),sanpham.getSoluong()*sanpham.getDongia()};
			model.addRow(obj);
		}
	}
	public boolean MaHD(String s) {
		return Pattern.matches(MaHD, s);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<SanPham> list=null;
		JFrame f = new JFrame();
		JPanel p = new GioHangGUI(list);
		f.setSize(700,700);
		f.add(p);
		f.setVisible(true);
		//f.pack();
		
	}

}
