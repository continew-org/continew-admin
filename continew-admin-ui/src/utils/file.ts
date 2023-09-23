export default function getFile(file: string | undefined) {
  if (file) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    if (
      !file.startsWith('http://') &&
      !file.startsWith('https://') &&
      !file.startsWith('blob:')
    ) {
      return `${baseUrl}/file/${file}`;
    }
  }
  return file;
}
