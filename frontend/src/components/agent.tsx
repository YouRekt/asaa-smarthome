import clsx from "clsx";
import type { Agent as AgentType } from "@/hooks/use-store";
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import useStore from "@/hooks/use-store";
import { useEffect, useState, useRef } from "react";
import { Button } from "@/components/ui/button";
import { ContextMenuTrigger } from "@/components/ui/context-menu";
import { ContextMenu, ContextMenuItem } from "@/components/ui/context-menu";
import { ContextMenuContent } from "@/components/ui/context-menu";
import { DialogFooter, DialogTrigger } from "@/components/ui/dialog";
import { DialogTitle } from "@/components/ui/dialog";
import { DialogHeader } from "@/components/ui/dialog";
import { DialogContent } from "@/components/ui/dialog";
import { Dialog } from "@/components/ui/dialog";
import { FormMessage } from "@/components/ui/form";
import { FormDescription } from "@/components/ui/form";
import { FormControl } from "@/components/ui/form";
import { FormField, FormItem, FormLabel } from "@/components/ui/form";
import { Form } from "@/components/ui/form";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";
import { SelectContent, SelectItem } from "@/components/ui/select";
import { SelectTrigger, SelectValue } from "@/components/ui/select";
import { Select } from "@/components/ui/select";
import useStomp from "@/hooks/use-stomp";

const performatives = [
	"ACCEPT_PROPOSAL",
	"AGREE",
	"CANCEL",
	"CFP",
	"CONFIRM",
	"DISCONFIRM",
	"FAILURE",
	"INFORM",
	"INFORM_IF",
	"INFORM_REF",
	"NOT_UNDERSTOOD",
	"PROPOSE",
	"QUERY_IF",
	"QUERY_REF",
	"REFUSE",
	"REJECT_PROPOSAL",
	"REQUEST",
	"REQUEST_WHEN",
	"REQUEST_WHENEVER",
	"SUBSCRIBE",
	"PROXY",
	"PROPAGATE",
	"UNKNOWN",
] as const;

const FormSchema = z.object({
	aid: z.string(),
	performative: z.enum(performatives),
	conversationId: z.string(),
	message: z.string(),
});

export const Agent = ({ agent }: { agent: AgentType }) => {
	const {
		agentMessages,
		setSelectedAgent,
		selectedAgent,
		setModalOpen,
		modalOpen,
		showErrors,
	} = useStore();
	const { sendMessage } = useStomp();
	const [open, setOpen] = useState(false);
	const timeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

	const myMessages = agentMessages[agent.aid];

	const isSensor = agent.name.includes("Sensor");

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			aid: agent.aid,
			performative: "INFORM",
			conversationId: "",
			message: "",
		},
	});

	function onSubmit(data: z.infer<typeof FormSchema>) {
		console.log(data);
		sendMessage("/app/agent-message", data);
		form.reset();
		setModalOpen(null);
	}

	useEffect(() => {
		if (myMessages && myMessages.length > 0) {
			setOpen(true);

			// Clear any existing timeout
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}

			// Set new timeout
			timeoutRef.current = setTimeout(() => {
				setOpen(false);
			}, 3000);
		}

		return () => {
			if (timeoutRef.current) {
				clearTimeout(timeoutRef.current);
			}
		};
	}, [myMessages]);

	return (
		<TooltipProvider>
			<Tooltip open={open && !selectedAgent && !modalOpen && !showErrors}>
				<Dialog
					open={modalOpen === agent.aid}
					onOpenChange={() => setModalOpen(null)}
				>
					<ContextMenu>
						<ContextMenuTrigger asChild>
							<TooltipTrigger asChild>
								<Button
									className={clsx(
										"flex flex-col gap-2 h-fit p-2 rounded-md text-white w-full",
										isSensor
											? "bg-zinc-600 hover:bg-zinc-700"
											: "bg-blue-800 hover:bg-blue-900"
									)}
									onClick={() => setSelectedAgent(agent.aid)}
								>
									<h1 className="text-2xl font-bold">
										{agent.name}
									</h1>
									<p className="text-sm">{agent.area}</p>
								</Button>
							</TooltipTrigger>
						</ContextMenuTrigger>
						<ContextMenuContent>
							<DialogTrigger asChild>
								<ContextMenuItem
									onSelect={() => setModalOpen(agent.aid)}
								>
									Send message
								</ContextMenuItem>
							</DialogTrigger>
						</ContextMenuContent>
					</ContextMenu>

					<DialogContent>
						<DialogHeader>
							<DialogTitle>
								Send message to {agent.name}
							</DialogTitle>
						</DialogHeader>
						<Form {...form}>
							<form
								onSubmit={form.handleSubmit(onSubmit)}
								className="space-y-6"
							>
								<FormField
									control={form.control}
									name="performative"
									render={({ field }) => (
										<FormItem>
											<FormLabel>Performative</FormLabel>
											<Select
												onValueChange={field.onChange}
												defaultValue={field.value}
											>
												<FormControl>
													<SelectTrigger>
														<SelectValue placeholder="Select a verified email to display" />
													</SelectTrigger>
												</FormControl>
												<SelectContent>
													{performatives.map(
														(performative) => (
															<SelectItem
																key={
																	performative
																}
																value={
																	performative
																}
															>
																{performative}
															</SelectItem>
														)
													)}
												</SelectContent>
											</Select>
											<FormDescription>
												Select the performative of the
												message
											</FormDescription>
											<FormMessage />
										</FormItem>
									)}
								/>
								<FormField
									control={form.control}
									name="conversationId"
									render={({ field }) => (
										<FormItem>
											<FormLabel>
												Conversation ID
											</FormLabel>
											<FormControl>
												<Input
													placeholder="Enter conversation ID"
													{...field}
												/>
											</FormControl>
											<FormDescription>
												Enter a conversation ID
											</FormDescription>
											<FormMessage />
										</FormItem>
									)}
								/>
								<FormField
									control={form.control}
									name="message"
									render={({ field }) => (
										<FormItem>
											<FormLabel>Message</FormLabel>
											<FormControl>
												<Input
													placeholder="Enter message"
													{...field}
												/>
											</FormControl>
											<FormDescription>
												Enter a message to send to{" "}
												{agent.name}
											</FormDescription>
											<FormMessage />
										</FormItem>
									)}
								/>
								<DialogFooter>
									<Button type="submit">Submit</Button>
								</DialogFooter>
							</form>
						</Form>
					</DialogContent>
				</Dialog>
				<TooltipContent
					side="right"
					className={clsx(
						"relative",
						isSensor ? "bg-zinc-600" : "bg-blue-800"
					)}
					arrowClassName={clsx(
						isSensor
							? "bg-zinc-600 fill-zinc-600"
							: "bg-blue-800 fill-blue-800"
					)}
				>
					{myMessages?.length ? (
						<div className="flex flex-col gap-1">
							{myMessages
								.slice(-3)
								.reverse()
								.map((msg, i) => (
									<div key={i} className="text-sm">
										[{msg.timestamp}] : {msg.message}
									</div>
								))}
						</div>
					) : (
						"No messages yet"
					)}
					<div className="absolute -top-1 -right-1 w-3 h-3 bg-green-700 rounded-full animate-ping" />
				</TooltipContent>
			</Tooltip>
		</TooltipProvider>
	);
};
