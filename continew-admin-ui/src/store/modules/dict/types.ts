export interface LabelValueState {
  label: string;
  value: any;
}

export interface DictState {
  name: string;
  detail: Array<LabelValueState>;
}
