export interface InputFieldFormProps {
  type: inputTypes | string;
  name: string;
  value?: string;
}

export type inputTypes = 'email' | 'password' | 'text';
