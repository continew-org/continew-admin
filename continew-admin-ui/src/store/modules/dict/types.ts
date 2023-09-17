export interface LabelValueState {
  label: string;
  value: any;
  color?: string;
}

export interface DictState {
  name: string;
  detail: Array<LabelValueState>;
}
