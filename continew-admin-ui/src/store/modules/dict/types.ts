export interface LabelValueState {
  label: string;
  value: any;
  color?: string;
}

export interface DictState {
  code: string;
  items: Array<LabelValueState>;
}
