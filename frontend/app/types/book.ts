/**
 * Library Project - Core Type Definitions
 * 包含书籍接口定义及初始化工厂函数
 */

export interface Book {
  id?: number
  title: string
  author: string
  isbn: string
  bookGenre: string
  availableStock: number
  rating: number
  description: string
  coverImageUrl?: string // 加上问号，允许 undefined
  publicationDate?: string // 加上问号，允许 undefined
}

/**
 * 核心功能：创建空书籍对象
 * 作用：用于“新增图书”弹窗的初始化，确保所有基础字段都有默认值，避免模板报错。
 */
export const createEmptyBook = (): Book => ({
  title: '',
  author: '',
  isbn: '',
  // 默认分类定为 FICTION，匹配后端枚举
  bookGenre: 'FICTION',
  availableStock: 0,
  rating: 0,
  description: '',
  coverImageUrl: '',
  // 默认日期为今天（格式化为 YYYY-MM-DD）
  publicationDate: new Date().toISOString().split('T')[0]
})