import type { RxStompConfig } from "@stomp/rx-stomp";
import { RxStomp, RxStompState } from "@stomp/rx-stomp";
import { enableMapSet } from "immer";
import { Subscription } from "rxjs";
import { map } from "rxjs/operators";
import { create } from "zustand";
import { immer } from "zustand/middleware/immer";

enableMapSet();

const stompConfig: RxStompConfig = {
	brokerURL: "ws://localhost:8080/ws",
	debug: (msg) => {
		console.log(new Date().toISOString(), msg);
	},
	heartbeatIncoming: 0,
	heartbeatOutgoing: 20000,
	reconnectDelay: 200,
};

type StompState = {
	rxStomp: RxStomp | null;
	isConnected: boolean;
	subscriptions: Map<string, Subscription>;
};

type StompActions = {
	connect: () => void;
	disconnect: () => void;
	subscribe: (
		destination: string,
		callback: (body: Record<string, unknown>) => void
	) => void;
	unsubscribe: (destination: string) => void;
	publish: (destination: string, body: Record<string, unknown>) => void;
};

export const useStomp = create<StompState & StompActions>()(
	immer((set, get) => ({
		rxStomp: null,
		isConnected: false,
		subscriptions: new Map(),

		connect: () => {
			const { rxStomp: existingStomp } = get();
			if (existingStomp?.connected) return;

			const rxStomp = new RxStomp();
			rxStomp.configure(stompConfig);

			// Track connection state and convert to boolean
			rxStomp.connectionState$.subscribe((state) => {
				const connected = state === RxStompState.OPEN;
				set((draft) => {
					draft.isConnected = connected;
				});
			});

			rxStomp.activate();

			set((draft) => {
				draft.rxStomp = rxStomp;
			});
		},

		disconnect: () => {
			const { rxStomp, subscriptions } = get();

			// Clean up all subscriptions
			subscriptions.forEach((subscription) => subscription.unsubscribe());

			if (rxStomp) {
				rxStomp.deactivate();
			}

			set((draft) => {
				draft.rxStomp = null;
				draft.isConnected = false;
				draft.subscriptions.clear();
			});
		},

		subscribe: (destination, callback) => {
			const { rxStomp, subscriptions } = get();
			if (!rxStomp) return;

			// Clean up existing subscription
			const existingSubscription = subscriptions.get(destination);
			if (existingSubscription) {
				existingSubscription.unsubscribe();
			}

			// Create new subscription with JSON parsing
			const subscription = rxStomp
				.watch(destination)
				.pipe(map((message) => JSON.parse(message.body)))
				.subscribe(callback);

			set((draft) => {
				draft.subscriptions.set(destination, subscription);
			});
		},

		unsubscribe: (destination) => {
			const { subscriptions } = get();
			const subscription = subscriptions.get(destination);

			if (subscription) {
				subscription.unsubscribe();
				set((draft) => {
					draft.subscriptions.delete(destination);
				});
			}
		},

		publish: (destination, body) => {
			const { rxStomp } = get();
			if (!rxStomp?.connected) {
				console.warn("Cannot publish: STOMP not connected");
				return;
			}

			rxStomp.publish({ destination, body: JSON.stringify(body) });
			console.log(`Published to ${destination}:`, body);
		},
	}))
);
