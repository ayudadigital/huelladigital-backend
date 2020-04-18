type typeInput = 'text' | 'email' | 'password';

export interface FieldFormProps {
  title: string;
  type: typeInput;
  name: string;
}
