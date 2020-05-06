export interface InputFieldFormProps {
  type: inputTypes;
  name: string;
  value?: string;
}

export type inputTypes = 'email' | 'password' | 'text';
