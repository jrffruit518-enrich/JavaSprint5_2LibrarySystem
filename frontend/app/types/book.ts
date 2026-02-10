export interface Book {
  id: number
  title: string
  author: string
  isbn: string
  bookGenre: string
  availableStock: number
  rating: number
  description: string
  coverImageUrl?: string
  publicationDate?: string
}
