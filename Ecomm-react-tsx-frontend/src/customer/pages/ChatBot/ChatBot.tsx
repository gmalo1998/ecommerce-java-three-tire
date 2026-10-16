
import { useEffect, useRef, useState } from "react";

import { Button, IconButton } from "@mui/material";
import SendIcon from "@mui/icons-material/Send";
import CloseIcon from "@mui/icons-material/Close";

import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import { chatBot } from "../../../Redux Toolkit/Customer/AiChatBotSlice";

import PromptMessage from "./PromptMessage";
import ResponseMessage from "./ResponseMessage";

interface ChatBotProps {
  handleClose: (e: any) => void;
  productId?: number;
}

const ChatBot = ({ handleClose, productId }: ChatBotProps) => {
  const dispatch = useAppDispatch();
  const [prompt, setPrompt] = useState("");

  const chatContainerRef = useRef<HTMLDivElement>(null);
  const { aiChatBot } = useAppSelector((store) => store);

  const handleGivePrompt = (e: any) => {
    e.stopPropagation();

    if (!prompt.trim()) return;

    dispatch(
      chatBot({
        prompt: { prompt },
        productId,
        userId: null,
      })
    );
  };

  const handlePromptChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPrompt(e.target.value);
  };

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [aiChatBot.messages]);

  return (
    <div className="rounded-lg">
      <div className="w-full lg:w-[40vw] h-[82vh] shadow-2xl bg-white z-50 rounded-lg">
        <div className="h-[12%] flex justify-between items-center px-5 bg-slate-100 rounded-t-lg">
          <div className="flex items-center gap-3">
            <h1 className="logo">Shopzy</h1>
            <p>Assistant</p>
          </div>

          <div>
            <IconButton onClick={handleClose} color="primary">
              <CloseIcon />
            </IconButton>
          </div>
        </div>

        <div className="h-[78%] p-5 flex flex-col py-5 px-5 overflow-y-auto custom-scrollbar">
          <p>
            Welcome to Shopzy AI Assistant, you can
            {productId
              ? ` query about this product: ${productId}`
              : " query about your cart and order history here"}
          </p>

          {aiChatBot.messages.map((item: any, index: number) =>
            item.role === "user" ? (
              <div
                ref={chatContainerRef}
                className="self-end"
                key={index}
              >
                <PromptMessage message={item.message} index={index} />
              </div>
            ) : (
              <div
                ref={chatContainerRef}
                className="self-start"
                key={index}
              >
                <ResponseMessage message={item.message} />
              </div>
            )
          )}

          {aiChatBot.loading && <p>Fetching data...</p>}
        </div>

        <div className="h-[10%] flex items-center">
          <input
            value={prompt}
            onChange={handlePromptChange}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                handleGivePrompt(e);
              }
            }}
            type="text"
            placeholder="Give your prompt"
            className="rounded-bl-lg pl-5 h-full w-full bg-slate-100 border-none outline-none"
          />

          <Button
            sx={{ borderRadius: "0 0 0.5rem 0" }}
            className="h-full"
            onClick={handleGivePrompt}
            variant="contained"
          >
            <SendIcon />
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ChatBot;

