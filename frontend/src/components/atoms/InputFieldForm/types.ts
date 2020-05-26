export interface InputFieldFormProps {
  type: inputTypes | string;
  name: string;
  value?: string;
  onChangeValue?: any;
}

export type inputTypes = 'email' | 'password' | 'text';
