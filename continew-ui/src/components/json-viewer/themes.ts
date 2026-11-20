export interface JsonColorTheme {
  /** Background color. */
  bg: string
  /** Default foreground / punctuation color. */
  fg: string
  /** Object property key color. */
  key: string
  /** String value color. */
  string: string
  /** Number value color. */
  number: string
  /** Boolean value color. */
  boolean: string
  /** Null value color. */
  null: string
  /** Punctuation color (braces, colons, commas). */
  punctuation: string
}

export const jsonThemes: Record<string, JsonColorTheme> = {
  andromeeda: {
    bg: '#23262E',
    fg: '#D5CED9',
    key: '#00E8C6',
    string: '#96E072',
    number: '#F39C12',
    boolean: '#EE5D43',
    null: '#EE5D43',
    punctuation: '#D5CED9'
  },
  'aurora-x': {
    bg: '#07090F',
    fg: '#bbbbbb',
    key: '#C792EA',
    string: '#C3E88D',
    number: '#F78C6C',
    boolean: '#F78C6C',
    null: '#F78C6C',
    punctuation: '#89DDFF'
  },
  'ayu-dark': {
    bg: '#0d1017',
    fg: '#bfbdb6',
    key: '#39BAE6',
    string: '#AAD94C',
    number: '#D2A6FF',
    boolean: '#D2A6FF',
    null: '#D2A6FF',
    punctuation: '#BFBDB6'
  },
  'ayu-light': {
    bg: '#f8f9fa',
    fg: '#5c6166',
    key: '#55B4D4',
    string: '#86B300',
    number: '#A37ACC',
    boolean: '#A37ACC',
    null: '#A37ACC',
    punctuation: '#5C6166'
  },
  'ayu-mirage': {
    bg: '#1f2430',
    fg: '#cccac2',
    key: '#5CCFE6',
    string: '#D5FF80',
    number: '#DFBFFF',
    boolean: '#DFBFFF',
    null: '#DFBFFF',
    punctuation: '#CCCAC2'
  },
  'catppuccin-frappe': {
    bg: '#303446',
    fg: '#c6d0f5',
    key: '#8CAAEE',
    string: '#A6D189',
    number: '#EF9F76',
    boolean: '#EF9F76',
    null: '#EF9F76',
    punctuation: '#949CBB'
  },
  'catppuccin-latte': {
    bg: '#eff1f5',
    fg: '#4c4f69',
    key: '#1E66F5',
    string: '#40A02B',
    number: '#FE640B',
    boolean: '#FE640B',
    null: '#FE640B',
    punctuation: '#7C7F93'
  },
  'catppuccin-macchiato': {
    bg: '#24273a',
    fg: '#cad3f5',
    key: '#8AADF4',
    string: '#A6DA95',
    number: '#F5A97F',
    boolean: '#F5A97F',
    null: '#F5A97F',
    punctuation: '#939AB7'
  },
  'catppuccin-mocha': {
    bg: '#1e1e2e',
    fg: '#cdd6f4',
    key: '#89B4FA',
    string: '#A6E3A1',
    number: '#FAB387',
    boolean: '#FAB387',
    null: '#FAB387',
    punctuation: '#9399B2'
  },
  'dark-plus': {
    bg: '#1E1E1E',
    fg: '#D4D4D4',
    key: '#9CDCFE',
    string: '#CE9178',
    number: '#B5CEA8',
    boolean: '#569CD6',
    null: '#569CD6',
    punctuation: '#D4D4D4'
  },
  dracula: {
    bg: '#282A36',
    fg: '#F8F8F2',
    key: '#8BE9FD',
    string: '#F1FA8C',
    number: '#BD93F9',
    boolean: '#BD93F9',
    null: '#BD93F9',
    punctuation: '#F8F8F2'
  },
  'dracula-soft': {
    bg: '#282A36',
    fg: '#F8F8F2',
    key: '#8BE9FD',
    string: '#F1FA8C',
    number: '#BD93F9',
    boolean: '#BD93F9',
    null: '#BD93F9',
    punctuation: '#F8F8F2'
  },
  'everforest-dark': {
    bg: '#2D353B',
    fg: '#D3C6AA',
    key: '#7FBBB3',
    string: '#A7C080',
    number: '#D699B6',
    boolean: '#D699B6',
    null: '#D699B6',
    punctuation: '#D3C6AA'
  },
  'everforest-light': {
    bg: '#FDF6E3',
    fg: '#5C6A72',
    key: '#3A94C5',
    string: '#8DA101',
    number: '#DF69BA',
    boolean: '#DF69BA',
    null: '#DF69BA',
    punctuation: '#5C6A72'
  },
  'github-dark': {
    bg: '#24292e',
    fg: '#e1e4e8',
    key: '#79B8FF',
    string: '#9ECBFF',
    number: '#79B8FF',
    boolean: '#79B8FF',
    null: '#79B8FF',
    punctuation: '#E1E4E8'
  },
  'github-dark-default': {
    bg: '#0d1117',
    fg: '#e6edf3',
    key: '#79C0FF',
    string: '#A5D6FF',
    number: '#79C0FF',
    boolean: '#79C0FF',
    null: '#79C0FF',
    punctuation: '#E6EDF3'
  },
  'github-dark-dimmed': {
    bg: '#22272e',
    fg: '#adbac7',
    key: '#6CB6FF',
    string: '#96D0FF',
    number: '#6CB6FF',
    boolean: '#6CB6FF',
    null: '#6CB6FF',
    punctuation: '#ADBAC7'
  },
  'github-dark-high-contrast': {
    bg: '#0a0c10',
    fg: '#f0f3f6',
    key: '#71B7FF',
    string: '#CEE3F8',
    number: '#71B7FF',
    boolean: '#71B7FF',
    null: '#71B7FF',
    punctuation: '#F0F3F6'
  },
  'github-light': {
    bg: '#fff',
    fg: '#24292e',
    key: '#005CC5',
    string: '#032F62',
    number: '#005CC5',
    boolean: '#005CC5',
    null: '#005CC5',
    punctuation: '#24292E'
  },
  'github-light-default': {
    bg: '#ffffff',
    fg: '#1f2328',
    key: '#0550AE',
    string: '#0A3069',
    number: '#0550AE',
    boolean: '#0550AE',
    null: '#0550AE',
    punctuation: '#1F2328'
  },
  'github-light-high-contrast': {
    bg: '#ffffff',
    fg: '#010409',
    key: '#0349B4',
    string: '#0E4F1F',
    number: '#0349B4',
    boolean: '#0349B4',
    null: '#0349B4',
    punctuation: '#010409'
  },
  'gruvbox-dark-hard': {
    bg: '#1d2021',
    fg: '#ebdbb2',
    key: '#83A598',
    string: '#B8BB26',
    number: '#D3869B',
    boolean: '#D3869B',
    null: '#D3869B',
    punctuation: '#EBDBB2'
  },
  'gruvbox-dark-medium': {
    bg: '#282828',
    fg: '#ebdbb2',
    key: '#83A598',
    string: '#B8BB26',
    number: '#D3869B',
    boolean: '#D3869B',
    null: '#D3869B',
    punctuation: '#EBDBB2'
  },
  'gruvbox-dark-soft': {
    bg: '#32302f',
    fg: '#ebdbb2',
    key: '#83A598',
    string: '#B8BB26',
    number: '#D3869B',
    boolean: '#D3869B',
    null: '#D3869B',
    punctuation: '#EBDBB2'
  },
  'gruvbox-light-hard': {
    bg: '#f9f5d7',
    fg: '#3c3836',
    key: '#427B58',
    string: '#79740E',
    number: '#8F3F71',
    boolean: '#8F3F71',
    null: '#8F3F71',
    punctuation: '#3C3836'
  },
  'gruvbox-light-medium': {
    bg: '#fbf1c7',
    fg: '#3c3836',
    key: '#427B58',
    string: '#79740E',
    number: '#8F3F71',
    boolean: '#8F3F71',
    null: '#8F3F71',
    punctuation: '#3C3836'
  },
  'gruvbox-light-soft': {
    bg: '#f2e5bc',
    fg: '#3c3836',
    key: '#427B58',
    string: '#79740E',
    number: '#8F3F71',
    boolean: '#8F3F71',
    null: '#8F3F71',
    punctuation: '#3C3836'
  },
  horizon: {
    bg: '#1C1E26',
    fg: '#D5D8DA',
    key: '#E95678',
    string: '#FAB795',
    number: '#F09483',
    boolean: '#F09483',
    null: '#F09483',
    punctuation: '#BBBBBB'
  },
  'horizon-bright': {
    bg: '#FDF0ED',
    fg: '#403C3D',
    key: '#DA103F',
    string: '#A86B00',
    number: '#F58C00',
    boolean: '#F58C00',
    null: '#F58C00',
    punctuation: '#403C3D'
  },
  houston: {
    bg: '#17191e',
    fg: '#eee8d5',
    key: '#4bf3c8',
    string: '#fedc56',
    number: '#6acff1',
    boolean: '#6acff1',
    null: '#6acff1',
    punctuation: '#EEE8D5'
  },
  'kanagawa-dragon': {
    bg: '#181616',
    fg: '#c5c9c5',
    key: '#87a987',
    string: '#98bb6c',
    number: '#d27e99',
    boolean: '#d27e99',
    null: '#d27e99',
    punctuation: '#9E9B93'
  },
  'kanagawa-lotus': {
    bg: '#f2ecbc',
    fg: '#545464',
    key: '#6693bf',
    string: '#6f894e',
    number: '#b35b79',
    boolean: '#b35b79',
    null: '#b35b79',
    punctuation: '#8A8980'
  },
  'kanagawa-wave': {
    bg: '#1f1f28',
    fg: '#dcd7ba',
    key: '#7e9cd8',
    string: '#98bb6c',
    number: '#d27e99',
    boolean: '#d27e99',
    null: '#d27e99',
    punctuation: '#9A9A82'
  },
  laserwave: {
    bg: '#27212E',
    fg: '#FFFFFF',
    key: '#40B4C4',
    string: '#FFE261',
    number: '#B381C5',
    boolean: '#B381C5',
    null: '#B381C5',
    punctuation: '#FFFFFF'
  },
  'light-plus': {
    bg: '#FFFFFF',
    fg: '#000000',
    key: '#0451A5',
    string: '#A31515',
    number: '#098658',
    boolean: '#0000FF',
    null: '#0000FF',
    punctuation: '#000000'
  },
  'material-theme': {
    bg: '#263238',
    fg: '#EEFFFF',
    key: '#F78C6C',
    string: '#C3E88D',
    number: '#F78C6C',
    boolean: '#89DDFF',
    null: '#89DDFF',
    punctuation: '#89DDFF'
  },
  'material-theme-darker': {
    bg: '#212121',
    fg: '#EEFFFF',
    key: '#F78C6C',
    string: '#C3E88D',
    number: '#F78C6C',
    boolean: '#89DDFF',
    null: '#89DDFF',
    punctuation: '#89DDFF'
  },
  'material-theme-lighter': {
    bg: '#FAFAFA',
    fg: '#90A4AE',
    key: '#F76D47',
    string: '#91B859',
    number: '#F76D47',
    boolean: '#39ADB5',
    null: '#39ADB5',
    punctuation: '#39ADB5'
  },
  'material-theme-ocean': {
    bg: '#0F111A',
    fg: '#BABED8',
    key: '#F78C6C',
    string: '#C3E88D',
    number: '#F78C6C',
    boolean: '#89DDFF',
    null: '#89DDFF',
    punctuation: '#89DDFF'
  },
  'material-theme-palenight': {
    bg: '#292D3E',
    fg: '#BABED8',
    key: '#F78C6C',
    string: '#C3E88D',
    number: '#F78C6C',
    boolean: '#89DDFF',
    null: '#89DDFF',
    punctuation: '#89DDFF'
  },
  'min-dark': {
    bg: '#1f1f1f',
    fg: '#b392f0',
    key: '#79B8FF',
    string: '#FFAB70',
    number: '#F8F8F8',
    boolean: '#F8F8F8',
    null: '#F8F8F8',
    punctuation: '#B392F0'
  },
  'min-light': {
    bg: '#ffffff',
    fg: '#24292eff',
    key: '#005CC5',
    string: '#22863A',
    number: '#005CC5',
    boolean: '#005CC5',
    null: '#005CC5',
    punctuation: '#24292EFF'
  },
  monokai: {
    bg: '#272822',
    fg: '#F8F8F2',
    key: '#66D9EF',
    string: '#CFCFC2',
    number: '#AE81FF',
    boolean: '#AE81FF',
    null: '#AE81FF',
    punctuation: '#F8F8F2'
  },
  'night-owl': {
    bg: '#011627',
    fg: '#d6deeb',
    key: '#7FDBCA',
    string: '#ECC48D',
    number: '#F78C6C',
    boolean: '#FF5874',
    null: '#FF5874',
    punctuation: '#7FDBCA'
  },
  'night-owl-light': {
    bg: '#FBFBFB',
    fg: '#403f53',
    key: '#0C969B',
    string: '#C96765',
    number: '#AA0982',
    boolean: '#BC5454',
    null: '#BC5454',
    punctuation: '#0C969B'
  },
  nord: {
    bg: '#2e3440ff',
    fg: '#d8dee9ff',
    key: '#8FBCBB',
    string: '#A3BE8C',
    number: '#B48EAD',
    boolean: '#81A1C1',
    null: '#81A1C1',
    punctuation: '#ECEFF4'
  },
  'one-dark-pro': {
    bg: '#282c34',
    fg: '#abb2bf',
    key: '#E06C75',
    string: '#98C379',
    number: '#D19A66',
    boolean: '#D19A66',
    null: '#D19A66',
    punctuation: '#ABB2BF'
  },
  'one-light': {
    bg: '#FAFAFA',
    fg: '#383A42',
    key: '#E45649',
    string: '#50A14F',
    number: '#986801',
    boolean: '#986801',
    null: '#986801',
    punctuation: '#383A42'
  },
  plastic: {
    bg: '#21252B',
    fg: '#A9B2C3',
    key: '#E06C75',
    string: '#98C379',
    number: '#D19A66',
    boolean: '#56B6C2',
    null: '#56B6C2',
    punctuation: '#A9B2C3'
  },
  poimandres: {
    bg: '#1b1e28',
    fg: '#a6accd',
    key: '#5DE4C7',
    string: '#A6ACCD',
    number: '#5DE4C7',
    boolean: '#5DE4C7',
    null: '#5DE4C7',
    punctuation: '#A6ACCD'
  },
  red: {
    bg: '#390000',
    fg: '#F8F8F8',
    key: '#F8F8F8',
    string: '#FE6262',
    number: '#FF9D9D',
    boolean: '#FF9D9D',
    null: '#FF9D9D',
    punctuation: '#F8F8F8'
  },
  'rose-pine': {
    bg: '#191724',
    fg: '#e0def4',
    key: '#9CCFD8',
    string: '#F6C177',
    number: '#EB6F92',
    boolean: '#31748F',
    null: '#31748F',
    punctuation: '#908CAA'
  },
  'rose-pine-dawn': {
    bg: '#faf4ed',
    fg: '#575279',
    key: '#56949F',
    string: '#EA9D34',
    number: '#B4637A',
    boolean: '#286983',
    null: '#286983',
    punctuation: '#797593'
  },
  'rose-pine-moon': {
    bg: '#232136',
    fg: '#e0def4',
    key: '#9CCFD8',
    string: '#F6C177',
    number: '#EB6F92',
    boolean: '#3E8FB0',
    null: '#3E8FB0',
    punctuation: '#908CAA'
  },
  'slack-dark': {
    bg: '#222222',
    fg: '#E6E6E6',
    key: '#78AF9F',
    string: '#F69D50',
    number: '#E9A362',
    boolean: '#7DB3DE',
    null: '#7DB3DE',
    punctuation: '#E6E6E6'
  },
  'slack-ochin': {
    bg: '#FFFFFF',
    fg: '#002339',
    key: '#B3694D',
    string: '#4F9062',
    number: '#174781',
    boolean: '#174781',
    null: '#174781',
    punctuation: '#002339'
  },
  'snazzy-light': {
    bg: '#FAFBFC',
    fg: '#565869',
    key: '#D8366D',
    string: '#2D9B27',
    number: '#E06011',
    boolean: '#D8366D',
    null: '#D8366D',
    punctuation: '#565869'
  },
  'solarized-dark': {
    bg: '#002B36',
    fg: '#839496',
    key: '#859900',
    string: '#2AA198',
    number: '#D33682',
    boolean: '#B58900',
    null: '#B58900',
    punctuation: '#839496'
  },
  'solarized-light': {
    bg: '#FDF6E3',
    fg: '#657B83',
    key: '#859900',
    string: '#2AA198',
    number: '#D33682',
    boolean: '#B58900',
    null: '#B58900',
    punctuation: '#657B83'
  },
  'synthwave-84': {
    bg: '#262335',
    fg: '#BBBBBB',
    key: '#FF7EDB',
    string: '#FF8B39',
    number: '#F97E72',
    boolean: '#F97E72',
    null: '#F97E72',
    punctuation: '#BBBBBB'
  },
  'tokyo-night': {
    bg: '#1a1b26',
    fg: '#a9b1d6',
    key: '#7AA2F7',
    string: '#9ECE6A',
    number: '#FF9E64',
    boolean: '#FF9E64',
    null: '#FF9E64',
    punctuation: '#A9B1D6'
  },
  vesper: {
    bg: '#101010',
    fg: '#b1b1b1',
    key: '#FFC799',
    string: '#99FFE4',
    number: '#FFC799',
    boolean: '#FFC799',
    null: '#FFC799',
    punctuation: '#B1B1B1'
  },
  'vitesse-black': {
    bg: '#000000',
    fg: '#dbd7caee',
    key: '#4D9375',
    string: '#C98A7D',
    number: '#4C9A91',
    boolean: '#4D9375',
    null: '#4D9375',
    punctuation: '#666666'
  },
  'vitesse-dark': {
    bg: '#121212',
    fg: '#dbd7caee',
    key: '#4D9375',
    string: '#C98A7D',
    number: '#4C9A91',
    boolean: '#4D9375',
    null: '#4D9375',
    punctuation: '#666666'
  },
  'vitesse-light': {
    bg: '#ffffff',
    fg: '#393a34',
    key: '#1E754F',
    string: '#B5695999',
    number: '#2F798A',
    boolean: '#1E754F',
    null: '#1E754F',
    punctuation: '#999999'
  }
}

/** Curated subset of popular themes for UI pickers and demos. */
export const popularThemes = [
  'github-dark',
  'github-light',
  'monokai',
  'dracula',
  'nord',
  'one-dark-pro',
  'catppuccin-mocha',
  'tokyo-night',
  'solarized-dark',
  'night-owl',
  'rose-pine',
  'gruvbox-dark-medium',
  'ayu-dark',
  'material-theme-ocean',
  'vitesse-dark'
] as const

export type PopularTheme = (typeof popularThemes)[number]
export type ShikiThemeName = keyof typeof jsonThemes
