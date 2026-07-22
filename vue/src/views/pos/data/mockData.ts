export interface Category {
  id: number
  name: string
  icon: string
  count?: number
}

export interface Product {
  id: number
  name: string
  price: number
  categoryId: number
  image: string
  stock: number
  barcode: string
  description?: string
}

export interface CartItem {
  product: Product
  quantity: number
  subtotal: number
}

export const categories: Category[] = [
  { id: 1, name: '全部商品', icon: 'Grid', count: 128 },
  { id: 2, name: '食品饮料', icon: 'Coffee', count: 45 },
  { id: 3, name: '日用百货', icon: 'ShoppingBag', count: 32 },
  { id: 4, name: '休闲零食', icon: 'Goblet', count: 28 },
  { id: 5, name: '生鲜果蔬', icon: 'Cherry', count: 23 },
  { id: 6, name: '烟酒茶', icon: 'Bottle', count: 18 },
  { id: 7, name: '调味品', icon: 'Box', count: 15 },
]

export const products: Product[] = [
  { id: 1, name: '可口可乐 330ml', price: 3.5, categoryId: 2, image: '', stock: 150, barcode: '6901234567890' },
  { id: 2, name: '百事可乐 330ml', price: 3.5, categoryId: 2, image: '', stock: 120, barcode: '6901234567891' },
  { id: 3, name: '农夫山泉 550ml', price: 2.0, categoryId: 2, image: '', stock: 200, barcode: '6901234567892' },
  { id: 4, name: '怡宝矿泉水 550ml', price: 2.0, categoryId: 2, image: '', stock: 180, barcode: '6901234567893' },
  { id: 5, name: '红牛功能饮料', price: 6.0, categoryId: 2, image: '', stock: 80, barcode: '6901234567894' },
  { id: 6, name: '统一冰红茶 500ml', price: 4.0, categoryId: 2, image: '', stock: 100, barcode: '6901234567895' },
  { id: 7, name: '康师傅绿茶 500ml', price: 4.0, categoryId: 2, image: '', stock: 90, barcode: '6901234567896' },
  { id: 8, name: '雀巢咖啡 罐装', price: 5.0, categoryId: 2, image: '', stock: 60, barcode: '6901234567897' },
  { id: 9, name: '维达纸巾 抽纸', price: 8.5, categoryId: 3, image: '', stock: 50, barcode: '6901234567898' },
  { id: 10, name: '清风卷纸 10卷', price: 12.9, categoryId: 3, image: '', stock: 40, barcode: '6901234567899' },
  { id: 11, name: '舒肤佳香皂', price: 5.5, categoryId: 3, image: '', stock: 70, barcode: '6901234567900' },
  { id: 12, name: '蓝月亮洗衣液 2kg', price: 28.0, categoryId: 3, image: '', stock: 30, barcode: '6901234567901' },
  { id: 13, name: '奥利奥饼干', price: 15.8, categoryId: 4, image: '', stock: 85, barcode: '6901234567902' },
  { id: 14, name: '乐事薯片', price: 8.0, categoryId: 4, image: '', stock: 95, barcode: '6901234567903' },
  { id: 15, name: '坚果每日', price: 25.0, categoryId: 4, image: '', stock: 45, barcode: '6901234567904' },
  { id: 16, name: '三只松鼠大礼包', price: 68.0, categoryId: 4, image: '', stock: 25, barcode: '6901234567905' },
  { id: 17, name: '新鲜苹果 500g', price: 8.8, categoryId: 5, image: '', stock: 100, barcode: '6901234567906' },
  { id: 18, name: '香蕉 500g', price: 5.9, categoryId: 5, image: '', stock: 120, barcode: '6901234567907' },
  { id: 19, name: '草莓 一盒', price: 25.0, categoryId: 5, image: '', stock: 40, barcode: '6901234567908' },
  { id: 20, name: '西兰花 500g', price: 6.8, categoryId: 5, image: '', stock: 60, barcode: '6901234567909' },
  { id: 21, name: '中华香烟', price: 65.0, categoryId: 6, image: '', stock: 30, barcode: '6901234567910' },
  { id: 22, name: '五粮液 52度', price: 350.0, categoryId: 6, image: '', stock: 15, barcode: '6901234567911' },
  { id: 23, name: '云南普洱茶', price: 88.0, categoryId: 6, image: '', stock: 20, barcode: '6901234567912' },
  { id: 24, name: '海天酱油 500ml', price: 15.8, categoryId: 7, image: '', stock: 55, barcode: '6901234567913' },
  { id: 25, name: '食用盐 500g', price: 3.5, categoryId: 7, image: '', stock: 200, barcode: '6901234567914' },
  { id: 26, name: '太太乐鸡精', price: 22.0, categoryId: 7, image: '', stock: 45, barcode: '6901234567915' },
]

export const navItems = [
  { id: 'home', name: '首页', icon: