const { pathsToModuleNameMapper } = require('ts-jest');

const {
  compilerOptions: { paths = {}, baseUrl = './' },
} = require('./tsconfig.json');
const environment = require('./webpack/environment');

module.exports = {
  transformIgnorePatterns: ['node_modules/(?!.*.mjs$|dayjs/esm)'],
  resolver: 'jest-preset-angular/build/resolvers/ng-jest-resolver.js',
  globals: {
    ...environment,
  },
  roots: ['<rootDir>', `<rootDir>/${baseUrl}`],
  modulePaths: [`<rootDir>/${baseUrl}`],
  setupFiles: ['jest-date-mock'],
  cacheDirectory: '<rootDir>/target/jest-cache', // Este também pode precisar de ajuste, se <rootDir> não for a raiz do Maven
  coverageDirectory: '<rootDir>/target/test-results/', // Este também pode precisar de ajuste
  moduleNameMapper: pathsToModuleNameMapper(paths, { prefix: `<rootDir>/${baseUrl}/` }),
  reporters: [
    'default',
    // Opcional: Se você ainda precisa do relatório jest-junit para outros fins,
    // ajuste o caminho para que ele também vá para o target raiz.
    // ['jest-junit', { outputDirectory: '../../target/test-results/', outputName: 'TESTS-results-jest.xml' }],

    // Este é o principal que precisamos ajustar para o SonarQube
    ['jest-sonar', { outputDirectory: '../../target/test-results/jest', outputName: 'TESTS-results-sonar.xml' }],
  ],
  testMatch: ['<rootDir>/src/main/webapp/app/**/@(*.)@(spec.ts)'],
  testEnvironmentOptions: {
    url: 'https://jhipster.tech',
  },
};
