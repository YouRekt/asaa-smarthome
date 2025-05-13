import { Client } from "@stomp/stompjs";
import { create } from "zustand";

type StompStore = {
	client: Client | null;
	setClient: (client: Client) => void;
	sendMessage: (destination: string, body: Record<string, unknown>) => void;
};

const useStomp = create<StompStore>((set, get) => ({
	client: null,
	setClient: (client) => set({ client }),
	sendMessage: (destination, body) => {
		const client = get().client;
		if (client?.connected) {
			client.publish({
				destination,
				body: JSON.stringify(body),
			});
		}
	},
}));

export default useStomp;
