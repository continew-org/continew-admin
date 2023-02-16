import { mergeConfig } from 'vite';
import eslint from 'vite-plugin-eslint';
import baseConfig from './vite.config.base';
import createSvgIcon from './plugin/svg-icon';

export default mergeConfig(
  {
    mode: 'development',
    server: {
      open: true,
      fs: {
        strict: true,
      },
    },
    plugins: [
      eslint({
        cache: false,
        include: ['src/**/*.ts', 'src/**/*.tsx', 'src/**/*.vue'],
        exclude: ['node_modules'],
      }),
      createSvgIcon(false),
    ],
  },
  baseConfig
);
