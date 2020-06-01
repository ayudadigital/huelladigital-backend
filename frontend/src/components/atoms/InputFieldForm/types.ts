export interface InputFieldFormProps {
  type: inputTypes | string;
  name: string;
  value?: string;
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => string | void;
  onBlur?: any;
}

export type inputTypes = 'email' | 'password' | 'text';
