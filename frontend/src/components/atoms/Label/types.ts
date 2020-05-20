export interface LabelProps {
  text: string | LabelType;
}

type LabelType = 'email' | 'password';
