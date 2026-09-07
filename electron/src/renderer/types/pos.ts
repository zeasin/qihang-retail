export interface InventoryInfo {
  skuId: string
  quantity: number
  availableQuantity: number
}

export interface InventoryBatch {
  id: number
  skuId: string
  batchNum: string
  currentQty: number
  barcode: string
  stockStatus: number
}

export interface Product {
  id: string
  name: string
  shortName?: string
  image?: string
  goodsNum?: string
  barCode?: string
  unitName?: string
  retailPrice?: number
  categoryId?: number
  skuList?: SkuItem[]
  [key: string]: any
}

export interface SkuItem {
  id: string
  goodsId?: string
  skuName?: string
  skuCode?: string
  barCode?: string
  colorValue?: string
  sizeValue?: string
  styleValue?: string
  retailPrice?: number
  inventory?: InventoryInfo
  batches?: InventoryBatch[]
  [key: string]: any
}

export interface CartItem {
  goodsId: string
  skuId: string
  name: string
  shortName: string
  skuName: string
  image: string
  price: number
  barCode: string
  skuCode: string
  batchNum: string
  quantity: number
}

export interface PayMethod {
  key: string
  name: string
  icon: string
}
