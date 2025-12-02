import commonjs from '@rollup/plugin-commonjs';
import resolve from '@rollup/plugin-node-resolve';
import typescript from '@rollup/plugin-typescript';
import cleanup from 'rollup-plugin-cleanup';
import pkg from './package.json' assert {type: 'json'};
import terser from "@rollup/plugin-terser";

export default [
  {
    input: './src/index.ts',
    rootDir: './src',
    output: [
      {
        file: pkg.main,
        format: 'cjs',
        sourcemap: true
      },
      {
        file: pkg.module,
        format: 'esm',
        sourcemap: true
      }
    ],
    plugins: [
        resolve(),
        typescript({ sourceMap: true }),
        commonjs({
          exclude: 'node_modules',
          ignoreGlobal: true
        }),
        terser(),
        cleanup({ comments: 'none' })
    ],
    external: Object.keys({ ...pkg.devDependencies, ...pkg.dependencies })
  }
];