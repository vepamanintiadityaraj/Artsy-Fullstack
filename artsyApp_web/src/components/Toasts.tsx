import { Toast } from "react-bootstrap";

interface ToastsProps {
  show: boolean;
  message: string;
  type: "success" | "danger";
  onClose: () => void;
}

const Toasts = ({ show, message, type, onClose }: ToastsProps) => {
  return (
    <Toast
      bg={type}
      show={show}
      onClose={onClose}
      style={{ width: "auto", minWidth: "220px" }}
    >
      <Toast.Header
        closeButton
        style={{ border: "none", padding: "0.75rem 1rem" }}
      >
        <strong className="me-auto">{message}</strong>
      </Toast.Header>
    </Toast>
  );
};

export default Toasts;
