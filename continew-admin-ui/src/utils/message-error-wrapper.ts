import { Message, MessageReturn } from '@arco-design/web-vue';

let messageInstance: MessageReturn | null;
const messageErrorWrapper = (options: any) => {
  if (messageInstance) {
    messageInstance.close();
  }
  messageInstance = Message.error(options);
};

export default messageErrorWrapper;
